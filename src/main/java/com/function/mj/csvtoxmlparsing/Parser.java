package com.function.mj.csvtoxmlparsing;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Parser {

    
    @FunctionName("ParsingReflection")
    public HttpResponseMessage parsingReflection(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        final String csv_input = request.getBody().get();
        try {

            
            List<String[]> data = getData(csv_input);
            if (data == null || data.size() == 0)
                throw new Exception("data is empty");
                
            Class[] params = new Class[data.get(0).length];
            Arrays.fill(params, String.class);
            String xml = "";

            Class dataclass = Class.forName("com.function.mj.csvtoxmlparsing.Score");
            
            
            // constructing and serializing objects to xml
            for (int i = 1; i < data.size(); i++) { // assuming first line contains headers
                Constructor c = dataclass.getDeclaredConstructor(params);
                Object o = c.newInstance((Object[])data.get(i));
                xml += convertToXML(o);
            }
            
            // printing out final results
            StringBuilder out = new StringBuilder();
            out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.append("<root>");
            out.append(xml);
            out.append("</root>");

            return request.createResponseBuilder(HttpStatus.OK).body(out.toString()).build();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }
    }

    public static List<String[]> getData(String csv_file) throws IOException, CsvException {
		CSVReader reader = new CSVReader(new StringReader((csv_file)));
		List<String[]> data = reader.readAll();
		reader.close();
		return data;
	}

    @FunctionName("ParsingCustom")
    public HttpResponseMessage parsingCustom(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        final String csv_input = request.getBody().get();
        try {

            List<String[]> data = getData(csv_input);
            if (data == null || data.size() == 0)
                throw new Exception("data is empty");
            
            String xml = "";

            String xmlreplace =   """
                <Data>
                    <name>$name</name>
                    <major>$major</major>
                    <institution>$institution</institution>
                    <score>$score</score>
                </Data>
                """;
            
            String xmlformat =  """
                <Data>
                    <name>%s</name>
                    <major>%s</major>
                    <institution>%s</institution>
                    <score>%s</score>
                </Data>
                """;

            // constructing and serializing objects to xml
            for (int i = 1; i < data.size(); i++) { // assuming first line contains headers

                xml += xmlreplace
                    .replace("$name", data.get(i)[0])
                    .replace("$major", data.get(i)[1])
                    .replace("$institution", data.get(i)[2])
                    .replace("$score", data.get(i)[3]);

                xml += String.format(xmlformat, data.get(i)[0], data.get(i)[1], data.get(i)[2], data.get(i)[3]);
            }

            // printing out final results
            StringBuilder out = new StringBuilder();
            out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.append("<root>");
            out.append(xml);
            out.append("</root>");

            
            return request.createResponseBuilder(HttpStatus.OK).body(out.toString()).build();
        } catch (Exception e) {
            // TODO: handle exception
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.fillInStackTrace()).build();
        }

    }

    
	public static String convertToXML(Object data) throws FileNotFoundException {
		//XStream xstream = new XStream();
        XStream xstream = new XStream() {
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new PackageStrippingMapper(next);
            }
        };
        xstream.processAnnotations(Score.class);
		String xml = xstream.toXML(data);
		return xml;
	}

    private static class PackageStrippingMapper extends MapperWrapper {
        public PackageStrippingMapper(Mapper wrapped) {
            super(wrapped);
        }

        public String serializedClass(Class type) {
            return type.getName().replaceFirst(".*\\.", "");
        }
    }

}
