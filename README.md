# A-Query-Engine-for-Web-Service-Compositions

## Introduction

The aim of this project is to create an engine for executing web service compositions where adding a new web service requires minimal effort. This is achieved by representing a web service in an XML file containing the description of the inputs and outputs of the web service along with a description of the components of its URL call, in addition to an XSLT file describing how to transform the XML response of the web service into tuples.

The engine views the call result of a web service as a table (set of tuples) and is capable of executing web service composition queries over these tables. In this report, we describe the development of this engine and along with a discussion about the shortcomings of this approach.

## Choice of Web Service API

We choose Google Maps API to test our implementation of the query engine for three main
reasons: (i) it is highly available, (ii) it is very easy to use and (iii) it provides many functions
that can be composed into something meaningful.

We define web services for the following four functions:

1. Geocoding, which is the process of converting addresses (like "1600 Amphitheatre Parkway, Mountain View, CA") into geographic coordinates (like latitude 37.423021 and longitude -122.083739). The signature of the function is geocodeio(?full address, ?location). Where the variable ?location takes values which are the latitude and longitude of a location separated by a comma.

2. Reverse geocoding, which is the process of converting geographic coordinates into a humanreadable address. The signature of the function is reverseGeocodeio(?location, ?full address).

3. Place search, which takes as input a location, radius and a type and outputs Google maps place identifiers of all the points of interest of the given type that fall within distance less than radius from the given location. The signature of the function is nearbySearchiiio(?location, ?type, ?radius, ?place id).

4. Place details, which takes as input a Google maps place identifier and returns the following details about the given place: name, type, phone number, full address, rating, website and whether the place is open. The signature of the function is placeDetailsiooooooo(?place id, ?name, ?type, ?phone number, ?full address, ?rating, ?website, ?open)
