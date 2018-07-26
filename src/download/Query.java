/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package download;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.lang.ArrayUtils;
import parsers.ParseResultsForWS;
import parsers.WebServiceDescription;

/**
 *
 * @author issa
 */
public class Query {

    private final String queryString;
    private final Atom headAtom;
    private ArrayList<Atom> bodyAtoms;

    // REGEXES
    private static final String TABLE_NAME_PATTERN = "(?:[a-zA-Z_$][a-zA-Z_$0-9]*)";
    private static final String VARIABLE_NAME_PATTERN = "(?:[a-zA-Z_$][a-zA-Z_$0-9]*)";
    private static final String VARIABLE_PATTERN = "(?:\\?" + VARIABLE_NAME_PATTERN + ")";
    private static final String LITERAL_PATTERN = "(?:[\"][^\"]*[\"])";
    private static final String FIELD_PATTERN = "(?:(?:" + LITERAL_PATTERN + ")|(?:" + VARIABLE_PATTERN + "))";
    private static final String INPUT_OUTPUT_SEQUENCE_PATTERN = "i*o*";
    private static final String BODY_ATOM_PATTERN = TABLE_NAME_PATTERN + "\\s*" + "\\^\\s*" + INPUT_OUTPUT_SEQUENCE_PATTERN + "\\s*\\(.+\\)";
    private static final String HEAD_ATOM_PATTERN = TABLE_NAME_PATTERN + "\\s*\\(.+\\)";

    public Query(String queryString) throws InvalidQueryFormatException {

        this.queryString = queryString;

        String[] headBody = queryString.split("<-");
        if (headBody.length != 2) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        String head = headBody[0].trim();
        String body = headBody[1].trim();

        this.headAtom = this.createHeadAtom(head);
        this.bodyAtoms = new ArrayList<>();
        for (String atomString : body.split("#")) {
            this.bodyAtoms.add(createBodyAtom(atomString.trim()));
        }
    }

    public String execute() throws InadmissibleQueryException, FileNotFoundException, IOException, Exception {
        // Check if query is admissible
        HashSet<String> seenVariables = new HashSet<>();
        for (Atom atom : this.bodyAtoms) {
            String[] fields = atom.getFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].startsWith("?") && !seenVariables.contains(fields[i])) {
                    if (atom.getInputOutputSequence().charAt(i) != 'o') {
                        throw new InadmissibleQueryException("The variable " + fields[i] + " in function call " + atom.getName() + " has not been bound.");
                    }
                }
                seenVariables.add(fields[i]);
            }
        }

        String outputFile = "";
        ArrayList<String> previousHeadVariables = null;
        ArrayList<String> currentHeadVariables = null;
        Atom previousAtom = null;
        int c = 0;
        for (Atom atom : this.bodyAtoms) {
            if (outputFile.equals("")) {
                WebService ws = WebServiceDescription.loadDescription(atom.getName());
                currentHeadVariables = new ArrayList<>(ws.headVariables);
                String fileWithCallResult = ws.getCallResult(atom.getInputFields());
                String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);
                outputFile = "result.xml";
                File f = new File(outputFile);
                f.createNewFile();
                FileChannel src = new FileInputStream(fileWithTransfResults).getChannel();
                FileChannel dest = new FileOutputStream(outputFile).getChannel();
                dest.transferFrom(src, 0, src.size());
                src.close();
                dest.close();
                previousHeadVariables = new ArrayList<>(currentHeadVariables);
            } else {
                WebService ws = WebServiceDescription.loadDescription(atom.getName());
                ArrayList<String[]> listOfTupleResult = ParseResultsForWS.showResultsNormal(outputFile);
                ArrayList<String[]> listOfTupleResult1 = new ArrayList<>();

                for (int i = 0; i < listOfTupleResult.size(); i++) {
                    String[] currentTuple = listOfTupleResult.get(i);
                    ArrayList<String> callArguments = new ArrayList<>();
                    ArrayList<String> neededStuff = new ArrayList<>();
                    for (String stuff : atom.getInputFields()) {
                        if (!stuff.startsWith("?")) {
                            callArguments.add(stuff);
                        } else {
                            callArguments.add(currentTuple[ArrayUtils.indexOf(currentHeadVariables.toArray(), stuff)]);
                        }
                    }
                    String fileWithCallResult = ws.getCallResult(callArguments.toArray(new String[callArguments.size()]));
                    String fileWithTransfResults = ws.getTransformationResult(fileWithCallResult);
                    ArrayList<String[]> listOfTupleResult2 = ParseResultsForWS.showResults(fileWithTransfResults, ws);
                    
                    // JOIN ON THE OUTPUT VARIABLES:
                    for (String[] tuple : listOfTupleResult2)
                    {
                        boolean toBeAdded = true;
                        for (int j = 0; j < ws.headVariables.size(); j++)
                            if (currentHeadVariables.contains(ws.headVariables.get(j)) && previousAtom.getInputOutputSequence().charAt(j) == 'o')
                                toBeAdded &= (tuple[j].equals(currentTuple[currentHeadVariables.indexOf(ws.headVariables.get(j))]));
                        
                        if (toBeAdded)
                            listOfTupleResult1.add(tuple);
                    }
                }
                
                previousHeadVariables = new ArrayList<>(currentHeadVariables);
                currentHeadVariables = new ArrayList<>(ws.headVariables);

                // SELECTION
                String[] fields = atom.getFields();
                ArrayList<Integer> selectionFields = new ArrayList<>();
                for (int i = 0; i < fields.length; i++) {
                    if (!fields[i].startsWith("?") && ws.sequence.charAt(i) != 'i') {
                        selectionFields.add(i);
                    }
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<RESULT>\n");
                for (String[] tuple : listOfTupleResult1) {
                    int len = atom.getInputFields().length;
                    boolean toBeWritten = true;
                    for (int i = 0; i < selectionFields.size(); i++) {
                        toBeWritten &= (tuple[selectionFields.get(i)].equals(fields[selectionFields.get(i)]));
                    }
                    if (toBeWritten) {
                        writer.write("<RECORD>\n");
                        for (int i = 0; i < tuple.length; i++) {
                            if (c == this.bodyAtoms.size() - 1) {
                                if (Arrays.asList(this.headAtom.getFields()).contains(currentHeadVariables.get(i))) {
                                    writer.write("<ITEM ANGIE-VAR=\"" + currentHeadVariables.get(i) + "\">");
                                    writer.write(tuple[i]);
                                    writer.write("</ITEM>\n");
                                }
                            } else {
                                writer.write("<ITEM ANGIE-VAR=\"" + currentHeadVariables.get(i) + "\">");
                                writer.write(tuple[i]);
                                writer.write("</ITEM>\n");
                            }
                        }
                        writer.write("</RECORD>\n");
                    }
                }
                writer.write("</RESULT>");
                writer.close();
            }
            c++;
            previousAtom = atom;
        }

        return null;
    }

    private Atom createBodyAtom(String atomString) throws InvalidQueryFormatException {
        if (!atomString.matches(BODY_ATOM_PATTERN)) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        String name = atomString.substring(0, atomString.indexOf("^")).trim();
        if (!name.matches(TABLE_NAME_PATTERN)) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        String inputOutputSequence = atomString.substring(atomString.indexOf("^") + 1, atomString.indexOf("(")).trim();
        String[] fields = atomString.substring(atomString.indexOf("(") + 1, atomString.indexOf(")")).split("(?x),(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        if (fields.length <= 0) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
            if (!fields[i].matches(FIELD_PATTERN)) {
                throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
            }
        }

        if (fields.length != inputOutputSequence.length()) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        return new Atom(name, inputOutputSequence, fields);
    }

    private Atom createHeadAtom(String atomString) throws InvalidQueryFormatException {
        if (!atomString.matches(HEAD_ATOM_PATTERN)) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        String name = atomString.substring(0, atomString.indexOf("(")).trim();
        if (!name.matches(TABLE_NAME_PATTERN)) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        String[] fields = atomString.substring(atomString.indexOf("(") + 1, atomString.indexOf(")")).split("(?x),(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        if (fields.length <= 0) {
            throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
        }

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
            if (!fields[i].matches(FIELD_PATTERN)) {
                throw new InvalidQueryFormatException("Invalid query format. Please input a query like name^ioo(?field, ?field, ?field) <- name^ioo(?field, ?field, ?field) # name^ioo(?field, ?field, ?field)");
            }
        }

        return new Atom(name, "", fields);
    }
}

class Atom {

    private final String name;
    private final String inputOutputSequence;
    private final String[] fields;

    public String getName() {
        return name;
    }

    public String getInputOutputSequence() {
        return inputOutputSequence;
    }

    public String[] getFields() {
        return fields;
    }

    public String[] getInputFields() {
        return Arrays.copyOfRange(this.fields, 0, inputOutputSequence.lastIndexOf("i") + 1);
    }

    public String[] getOutputFields() {
        return Arrays.copyOfRange(this.fields, inputOutputSequence.lastIndexOf("i") + 1, this.fields.length);
    }

    public Atom(String name, String inputOutputSequence, String[] fields) {
        this.name = name;
        this.inputOutputSequence = inputOutputSequence;
        this.fields = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].startsWith("?")) {
                this.fields[i] = fields[i];
            } else {
                this.fields[i] = fields[i].substring(1, fields[i].length() - 1);
            }
        }
    }
}

class InvalidQueryFormatException extends Exception {

    public InvalidQueryFormatException(String message) {
        super(message);
    }
}

class InadmissibleQueryException extends Exception {

    public InadmissibleQueryException(String message) {
        super(message);
    }
}