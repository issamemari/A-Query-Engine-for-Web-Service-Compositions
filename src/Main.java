import download.Query;

public class Main {

    
    public static void main(String[] args) throws Exception {
        Query q = new Query("result(?name, ?phone_number, ?full_address) <- geocode^io(\"212 rue de Tolbiac\", ?location) # nearbySearch^iiio(?location, \"bakery\", \"500\", ?place_id) # placeDetails^iooooooo(?place_id, ?name, ?type, ?phone_number, ?full_address, ?rating, ?website, \"true\")");
        Query q1 = new Query("result(?name, ?phone_number, ?full_address) <- reverseGeocode^io(\"48.8260360,2.3459570\", ?full_address)");
        Query q2 = new Query("result(?name, ?phone_number, ?full_address) <- geocode^io(\"212 rue de Tolbiac\", ?location)");
        q.execute();
    }
}