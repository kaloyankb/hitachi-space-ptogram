package launchDateApplication;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.logging.Logger;


public class Application {
    private static final String PAR_TEMP = "Temperature";
    private static final String PAR_WIND = "Wind";
    private static final String PAR_HUM = "Humidity";
    private static final String PAR_PREC = "Precipitation";
    private static final String PAR_LIGHTING = "Lighting";
    private static final String PAR_CLOUDS = "Clouds";
    private static final String VAL_CUMUL = "Cumulus";
    private static final String VAL_NIMB = "Nimbus";
    public static boolean shouldLaunch(String filePath) throws Exception{
        Scanner scanner = new Scanner(new File(filePath));
        List<List<String>> dataFromFile = new ArrayList<>();
        int count = 0;
        while(scanner.hasNextLine()) {
            count++;
            if(count == 1) { //skips header;
                scanner.nextLine();
                continue;
            }
            dataFromFile.add(getDataFromLine(scanner.nextLine()));
        }

        scanner.close();
        String launchDates = "";
        String paramName;
        File file = new File(".\\new_CSV_files\\WeatherReport.csv");
        try {
            FileWriter outputFile = new FileWriter(file);

            CSVWriter writer = new CSVWriter(outputFile);
            String[] header = {"Parameter/Aggregate", "Min", "Max", "Aver", "Med", "Appropriate Dates"};
            writer.writeNext(header);

            for (List<String> paramData : dataFromFile) {
                paramName = paramData.get(0);
                List<String> forCSVFile = new ArrayList<>();
                forCSVFile.add(paramName);
                forCSVFile.addAll(generateAggregates(paramData, paramName));
                launchDates = generateLaunchDates(paramData, paramName);
                forCSVFile.add(launchDates);
                writer.writeNext(forCSVFile.toArray(new String [0]));
            }
            writer.close();
        }
        catch (IOException e) {
            System.out.println("Error while creating a CSV file!");
        }
        return true;
    }

    public static List<String> generateAggregates(List<String> data, String paramName) {
        List<String> aggregates = new ArrayList<>();
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        double med = 0;
        int aver = 0;
        int sum = 0;
        for(int i = 1; i < data.size(); i++) { //starting from 1 - ignoring the header
            int curr = 0;
            try {
                curr = Integer.parseInt(data.get(i).trim());
            } catch (NumberFormatException e) {
                if(!paramName.equals(PAR_LIGHTING) && !paramName.equals(PAR_CLOUDS)) {
                    System.out.println("Error - There is a non numeric character in a number data cell");
                    return null;
                }
                String blank = "";
                aggregates.add(blank);//min
                aggregates.add(blank);//max
                aggregates.add(blank);//average
                aggregates.add(blank);//median
                return aggregates;
            }
            if(curr < min) min = curr;
            if(curr > max) max = curr;
            sum += curr;
        }
        aver = sum / data.size();
        med = generateMedian(data);
        aggregates.add(String.valueOf(min));
        aggregates.add(String.valueOf(max));
        aggregates.add(String.valueOf(aver));
        aggregates.add(String.valueOf(med));
        return aggregates;
    }

    public static double generateMedian(List<String> data) {//check if data is numeric
        double med = 0.0;
        List<String> dataCpy = new ArrayList<>();
        for(int i = 1; i < data.size(); i++) {
            dataCpy.add(data.get(i).replaceAll(" ", ""));//removing the heading interval (after each comma)
        }
        int dataSize = dataCpy.size();

        Collections.sort(dataCpy, Comparator.comparing(Integer::valueOf));
        if(dataSize % 2 == 0) {
            double midNumber1 = Double.valueOf(dataCpy.get(dataSize / 2 - 1));
            double midNumber2 = Double.valueOf(dataCpy.get(dataSize / 2));
            med = (midNumber1 + midNumber2) / 2;
        } else {
            med = Integer.parseInt(dataCpy.get(dataSize / 2));
        }
        return med;
    }

    public static String generateLaunchDates(List<String> data, String par) {
        List<Integer> launchDates = new ArrayList<Integer>();
        int currNumber = 0;
        int date = -1;
        int min = Integer.MAX_VALUE;
        for(String s: data) {
            date++;
            if(date == 0) { //skips the header;
                continue;
            }
            switch (par) {
                case PAR_TEMP:
                    currNumber = Integer.parseInt(s.trim());
                    if(currNumber >= 2 && currNumber <= 31) {
                        launchDates.add(date);
                    }
                    break;
                case PAR_WIND:
                    currNumber = Integer.parseInt(s.trim());
                    if(currNumber > 10) {
                        continue;
                    }
                    if(currNumber > min) {
                       continue;
                    } else if (currNumber == min){
                        launchDates.add(date);
                    } else { //currNumber < min
                        launchDates.clear();
                        min = currNumber;
                        launchDates.add(date);
                    }
                    break;
                case PAR_HUM:
                    currNumber = Integer.parseInt(s.trim());
                    if(currNumber >= 60) {
                        continue;
                    }
                    if(currNumber > min) {
                        continue;
                    } else if (currNumber == min){
                        launchDates.add(date);
                    } else { //currNumber < min
                        launchDates.clear();
                        min = currNumber;
                        launchDates.add(date);
                    }
                    break;
                case PAR_PREC:
                    currNumber = Integer.parseInt(s.trim());
                    if(currNumber == 0) {
                        launchDates.add(date);
                    }
                    break;
                case PAR_LIGHTING:
                    if(s.trim().equals("No")) {
                        launchDates.add(date);
                    }
                    break;
                case PAR_CLOUDS:
                    if(!s.trim().equals(VAL_CUMUL) && !s.trim().equals(VAL_NIMB)) {
                        launchDates.add(date);
                    }
                    break;
                default:
                    System.out.println("Error - Unknown parameter");
                    return null;
            }
        }
        return launchDates.toString();
    }

    public static List<String> getDataFromLine(String line) {
        List<String> values = new ArrayList<>();
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(",");
        while (scanner.hasNext()) {
            values.add(scanner.next());
        }
        scanner.close();
        return values;
    }
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hello, This is the Hitachi SPACE Program Application!" +
                "\nPlease enter weather information CSV file:");
        String CSVfile = scanner.nextLine();
        System.out.println("Please enter email sender, respective password and email recipient!" +
                "\nWe are going to send a weather report to that email recipient!\n");
        String from = scanner.nextLine();
        String password = scanner.nextLine();
        String to = scanner.nextLine();
        shouldLaunch(CSVfile);
        JavaMailUtil.sendMail(from, password, to);
    }
}
