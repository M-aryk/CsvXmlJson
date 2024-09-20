package ru.netology;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, ParseException {
        List<Employee> staff = new ArrayList<>();
        staff.add(new Employee(1, "John", "Smith", "USA", 25));
        staff.add(new Employee(2, "Ivan", "Petrov", "RU", 23));

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};

        String fileNameCsv = "data.csv";
        String fileNameXml = "data.xml";
        createCsv(staff, fileNameCsv, columnMapping);

        List<Employee> listCsv = parseCSV(columnMapping, fileNameCsv);
        String json = listToJson(listCsv);
        writeString(json, "data.json");

        List<Employee> listXml = parseXML(fileNameXml);
        String xml = listToJson(listXml);
        writeString(xml, "data.json2");

        String jsonRead = readToString("new_data.json");
        List<Employee> listJson = jsonToList(jsonRead);
        for (Employee employee : listJson) {
            System.out.println(employee.toString());
        }
    }

    private static List<Employee> jsonToList(String jsonRead) throws ParseException {
        ArrayList<Employee> listFromJson = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        JSONArray employee = (JSONArray) jsonParser.parse(jsonRead);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        for (Object employees : employee) {
            listFromJson.add(gson.fromJson(employees.toString(), Employee.class));
        }
        return listFromJson;
    }

    private static String readToString(String fileName) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String file;
            while ((file = br.readLine()) != null) {
                sb.append(file);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }

    private static List<Employee> parseXML(String fileName) throws ParserConfigurationException, IOException, SAXException {
        List<Employee> staff = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(fileName));
        document.getDocumentElement().normalize();
        NodeList node = document.getElementsByTagName("employee");
        for (int i = 0; i < node.getLength(); i++) {
            staff.add(getEmployee(node.item(i)));
        }
        return staff;
    }

    private static Employee getEmployee(Node node) {
        Employee employee = new Employee();
        if (Node.ELEMENT_NODE == node.getNodeType()) {
            Element element = (Element) node;
            employee.setId(Long.parseLong(getTagValue("id", element)));
            employee.setFirstName(getTagValue("firstName", element));
            employee.setLastName(getTagValue("lastName", element));
            employee.setCountry(getTagValue("country", element));
            employee.setAge(Integer.parseInt(getTagValue("age", element)));
        }
        return employee;
    }

    private static String getTagValue(String id, Element element) {
        NodeList nodeList = element.getElementsByTagName(id).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    private static void createCsv(List<Employee> staff, String fileName, String[] columnMapping) {
        ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(Employee.class);
        strategy.setColumnMapping(columnMapping);

        try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
            StatefulBeanToCsv<Employee> sbc = new StatefulBeanToCsvBuilder<Employee>(writer)
                    .withMappingStrategy(strategy)
                    .build();
            sbc.write(staff);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            e.printStackTrace();
        }
    }

    private static void writeString(String json, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(json);
            fileWriter.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static String listToJson(List<Employee> staff) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.toJson(staff);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> staff = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            staff = csv.parse();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
        return staff;
    }
}
