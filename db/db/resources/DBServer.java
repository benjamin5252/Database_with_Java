import DBExceptions.*;

import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.*;

class DBServer
{
    ArrayList<String> inputCommands = new ArrayList<String>();
    ArrayList<String> outputData = new ArrayList<String>();
    DBModel model = new DBModel();

    public DBServer(int portNumber)
    {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Server Listening");
            while(true) processNextConnection(serverSocket);
        } catch(IOException ioe) {
            System.err.println(ioe);
        }
    }

    private void processNextConnection(ServerSocket serverSocket)
    {
        try {
            Socket socket = serverSocket.accept();
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Connection Established");
            while(true) processNextCommand(socketReader, socketWriter);
        } catch(IOException ioe) {
            System.err.println(ioe);
        } catch(NullPointerException npe) {
            System.out.println("Connection Lost");
        }
    }

    private void processNextCommand(BufferedReader socketReader, BufferedWriter socketWriter) throws IOException, NullPointerException
    {
        String incomingCommand = socketReader.readLine();

        //add paddings around the special characters, making it easier to parse
        incomingCommand = paddingSpecialStr(incomingCommand, "(");
        incomingCommand = paddingSpecialStr(incomingCommand, ")");
        incomingCommand = paddingSpecialStr(incomingCommand, ",");
        incomingCommand = paddingSpecialStr(incomingCommand, ";");
        incomingCommand = paddingSpecialStr(incomingCommand, "\'");
        incomingCommand = paddingSpecialStr(incomingCommand, "\"");
        incomingCommand = paddingSpecialStr(incomingCommand, ">");
        incomingCommand = paddingSpecialStr(incomingCommand, "<");
        incomingCommand = paddingSpecialStr(incomingCommand, "=");
        incomingCommand = paddingSpecialStr(incomingCommand, "!");

        //use scanner to scan the incomingCommands into split strings, and save into ArrayList<String> inputCommands
        try {
            Scanner commandScanner = new Scanner(incomingCommand);
            while (commandScanner.hasNext()){

                String command = commandScanner.next();
                inputCommands.add(command);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            //check if there is a ; in the end of input command or not
            if(!inputCommands.get(inputCommands.size() - 1).equals(";")){
                throw new DBParseException("Semi colon missing at end of line");
            }

            System.out.println("Received message: " + incomingCommand);

            //us DBParser to parse the inputCommands
            //the parser will update the file with DBFileIo
            //and update the data to output in DBModel
            new DBParser(model).parseCommands(inputCommands);
            socketWriter.write("[OK]");
            //clear the inputCommands in the memory for the next inputCommands
            inputCommands.clear();

            //catch and print the error message both to server and client
        }catch (DBException e){
            System.out.println(e.toString());
            socketWriter.write(e.toString());
            inputCommands.clear();
        }
        //if there is some data in the model.outputData, than print the data to client's terminal
        if(!model.outputData.isEmpty()){
            dataOutPut(socketWriter);
        }
        model.outputData.clear();
        socketWriter.write("\n" + ((char)4) + "\n");

        socketWriter.flush();

    }

    //add paddings around the specific characters
    private String paddingSpecialStr(String incomingCommand, String SpecialStr){
        if(incomingCommand.contains(SpecialStr)){
            incomingCommand = incomingCommand.replace(SpecialStr, " " + SpecialStr + " ");
        }

        return  incomingCommand;
    }

    //output the data in model.outputData
    private void dataOutPut(BufferedWriter socketWriter) throws IOException, NullPointerException {
        socketWriter.write("\n");
        //calculate the width of every column
        int[] cellLength = new int[model.outputData.get(0).size()];
        for(int i = 0; i < model.outputData.get(0).size(); i++){
            int maxLength = 0;
            for(int j = 0; j < model.outputData.size(); j++){
                if(model.outputData.get(j).get(i).length() > maxLength){
                    maxLength = model.outputData.get(j).get(i).length();
                }
            }
            //4 is the spaces between two columns
            cellLength[i] = maxLength + 4;
        }
        for(ArrayList<String> j : model.outputData){
            for(int i = 0; i < model.outputData.get(0).size(); i++){
                j.set(i, j.get(i).replace("\"", " "));
                socketWriter.write(j.get(i));
                for(int k = j.get(i).length(); k < cellLength[i]; k++){
                    socketWriter.write(" ");
                }
            }
            socketWriter.write("\n");
        }
    }


    public static void main(String args[])
    {
        DBServer server = new DBServer(8888);
    }

}
