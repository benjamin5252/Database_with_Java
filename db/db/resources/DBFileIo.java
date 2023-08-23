import java.io.*;
import java.util.*;

import DBExceptions.*;

//a class responsible for all of the io to the file
public class DBFileIo {

    ArrayList<DBTable> tableLoaded;
    private ArrayList<String> strBuffer = new ArrayList<String>();

    public DBFileIo() {
        tableLoaded = new ArrayList<DBTable>();
        //ensure that there is a folder called DBData to keep the database data
        addDataDir();
    }

    //load the data of table according to the database name and table name
    public DBTable loadTable(String DBName, String TBName) throws DBException {
        if(DBName == null){
            throw new DBIoException("No database used");
        }
        String name = "DBData" + File.separator + DBName + File.separator + TBName + ".tab";
        File fileToOpen = new File(name);
        DBTable tab = new DBTable(TBName);
        //when the table is not in the specific folder, throw an exception
        if(!fileToOpen.exists()){
            throw new DBIoException("Table does not exist");
        }
        try {
            //use scanner to scan the contents, skipping the spaces in text
            Scanner scanTable = new Scanner(fileToOpen);
            Scanner scanColName = new Scanner(scanTable.nextLine());
            //scan the first line of the table text as the column data of the table
            while (scanColName.hasNext()){
                String data = scanColName.next();
                tab.addColumn(data);
            }
            //scan the rest data as the contents of the table
            while (scanTable.hasNext()){
                ArrayList<String> dataRow = new ArrayList<String>();
                Scanner scanDataRow = new Scanner(scanTable.nextLine());
                while(scanDataRow.hasNext()){
                    dataRow.add(scanDataRow.next());
                }
                while(dataRow.size() < tab.column.size()){
                    dataRow.add("");
                }
                tab.addDataRow(dataRow);
            }
        } catch (Exception e) {
            throw new DBIoException("Fail to load the table");
        }
        return tab;
    }


    //method to add a new table into the files
    public void addTable(String DBName, DBTable tab) throws DBException {
        if(DBName == null){
            throw new DBIoException("No database used");
        }
        String name = "DBData" + File.separator + DBName + File.separator + tab.name + ".tab";
        String DBFileName = "DBData" + File.separator + DBName + File.separator + DBName + ".db";
        File fileToWrite = new File(name);
        File DBToUpdate  = new File(DBFileName);
        try{
            //if the table is not in the specific folder, create one
            if(!fileToWrite.exists()){
                FileWriter DBWriter = new FileWriter(DBToUpdate);
                //a method hasTable() not only return if there is a table in the database,
                // but also update all of the table names into the strBuffer
                //here, I use the strBuffer-update function
                hasTable(DBName, tab.name);
                for(String i : strBuffer){
                    DBWriter.write(i + "\n");
                }
                DBWriter.write(tab.name + "\n");
                DBWriter.flush();
                DBWriter.close();
                //clean the strBuffer after use
                strBuffer.clear();
                FileWriter writer = new FileWriter(fileToWrite);
                //write the column data
                for (String i : tab.column){
                    writer.write(i + "\t" + "\t");
                }
                writer.write("\n");
                //write the rest content data
                for(ArrayList<String> j : tab.data){
                    for(String i : j){
                        writer.write(i + "\t" + "\t");
                    }
                    writer.write("\n");

                }
                writer.flush();
                writer.close();
            }else{
                throw new DBIoException(tab.name + " is already exist");
            }
        }catch (DBException e){
            throw e;
        }catch(Exception e){
            throw new DBIoException("fail to save Table " + tab.name);
        }
    }

    //update the existing table with the input DBTable data
    public void updateTable(String DBName, DBTable TBToUpdate) throws DBException{
        if(DBName == null){
            throw new DBIoException("No database used");
        }
        String name = "DBData" + File.separator + DBName + File.separator + TBToUpdate.name + ".tab";
        File fileToWrite = new File(name);
        try{
            //if there is no such table to update, throw an exception
            if(!fileToWrite.exists()){
                throw new DBIoException("Table does not exist");
            }else{
                FileWriter writer = new FileWriter(fileToWrite);
                for (String i : TBToUpdate.column){
                    writer.write(i + "\t" + "\t");
                }
                writer.write("\n");
                for(ArrayList<String> j : TBToUpdate.data){
                    for(String i : j){
                        writer.write(i + "\t" + "\t");
                    }
                    writer.write("\n");

                }
                writer.flush();
                writer.close();
            }

        }catch (DBException e){
            throw e;
        }catch (Exception e){
            throw new DBIoException("fail to update Table " + TBToUpdate.name);
        }
    }

    //a method to delete the table from files
    public void dropTable(String DBName, String TBName) throws DBException{
        if(DBName == null){
            throw new DBIoException("No database used");
        }
        String name = "DBData" + File.separator + DBName + File.separator + TBName + ".tab";
        String DBFileName = "DBData" + File.separator + DBName + File.separator + DBName + ".db";
        File fileToRM = new File(name);
        File DBToUpdate  = new File(DBFileName);
        try{
            //if there is no such table, throw an exception
            if(!fileToRM.exists()){
                throw new DBIoException("Table does not exist");
            }else{
                //update the table list in the database
                FileWriter DBWriter = new FileWriter(DBToUpdate);
                //a method hasTable() not only return if there is a table in the database,
                // but also update all of the table names into the strBuffer
                //here, I use the strBuffer-update function
                hasTable(DBName, TBName);
                strBuffer.remove(TBName);
                for(String i : strBuffer){
                    DBWriter.write(i + "\n");
                }
                DBWriter.flush();
                DBWriter.close();
                strBuffer.clear();
                //delete the corresponding table file
                rmDirectory(fileToRM);
            }
        }catch(DBException e){
            throw e;
        }catch(Exception e){
            throw new DBIoException("fail to drop Table " + TBName);
        }

    }
    //a method to add a database to the files, if there is no such database
    public void addDatabase(String DBName) throws DBException{
        String name = "DBData" + File.separator + "databases" + ".dbs";
        File fileToWrite = new File(name);
        String dir = "DBData" + File.separator + DBName;
        File dirToMk = new File(dir);
        try{
            if(hasDatabase(DBName)){
                throw new DBIoException(DBName + " already exist");
            }else{
                FileWriter writer = new FileWriter(fileToWrite);
                dirToMk.mkdir();
                for(String i : strBuffer){
                    writer.write(i + "\n");
                }
                writer.write(DBName + "\n");
                strBuffer.clear();
                writer.flush();
                writer.close();
            }

        }catch (DBException e){
            throw e;
        }catch(Exception e){
            throw new DBIoException("fail to add database: " + DBName);
        }
    }

    //a method to delete the database from the files
    public void dropDatabase(String DBName) throws DBException{
        String name = "DBData" + File.separator + "databases" + ".dbs";
        File fileToWrite = new File(name);
        String dir = "DBData" + File.separator + DBName;
        File dirToRM = new File(dir);
        try{
            //if there is no such database, throw an exception
            if(!hasDatabase(DBName)){
                throw new DBIoException("Unknown database");
            }else{
                FileWriter writer = new FileWriter(fileToWrite);
                //remove the database name from the buffer
                //and write it into the file for the names of all databases
                strBuffer.remove(DBName);
                for(String i : strBuffer){
                    writer.write(i + "\n");
                }
                //remove the database folder recursively, which remove the tables in it in the same time
                rmDirectory(dirToRM);
                strBuffer.clear();
                writer.flush();
                writer.close();
            }
        }catch(DBException e){
            throw e;
        }catch(Exception e){
            throw new DBIoException("fail to drop database: " + DBName);
        }
    }

    //a method to remove directory recursively
    boolean rmDirectory(File directoryToRM) {
        File[] contents = directoryToRM.listFiles();
        if (contents != null) {
            for (File file : contents) {
                rmDirectory(file);
            }
        }
        return directoryToRM.delete();
    }

    //a method to check the database name is on the name list file of databases or not.
    //in the mean time, the list of databases is also updated into the strBuffer for further use
    public boolean hasDatabase(String DBName) throws DBException{
        String name = "DBData" + File.separator + "databases" + ".dbs";
        File fileToRead = new File(name);
        try{
            if(fileToRead.exists()){
                Scanner scanDB = new Scanner(fileToRead);
                while (scanDB.hasNext()){
                    String data = scanDB.next();
                    strBuffer.add(data);
                }
            }
            if(strBuffer.contains(DBName)){
                return true;
            }
            return false;
        }catch (Exception e){
            throw new DBIoException("Fail to check the existence of the database");
        }

    }

    //a method to check the table name is on the name list file of tables in the database or not.
    //in the mean time, the list of tables is also updated into the strBuffer for further use
    public boolean hasTable(String DBName, String TBName) throws DBException{
        if(DBName == null){
            throw new DBIoException("No database used");
        }
        String name = "DBData" + File.separator + DBName + File.separator + DBName + ".db";
        File fileToRead = new File(name);
        try{
            if(fileToRead.exists()){
                Scanner scanDB = new Scanner(fileToRead);
                while (scanDB.hasNext()){
                    String data = scanDB.next();
                    strBuffer.add(data);
                }
            }
            if(strBuffer.contains(TBName)){
                return true;
            }
            return false;
        }catch (Exception e){
            throw new DBIoException("Fail to check the existence of the table");
        }

    }

    //method to ensure that there is a folder called DBData to keep the database data
    public void addDataDir() {
        String dir ="DBData";
        File dirToMk = new File(dir);
        if(!dirToMk.exists()){
            dirToMk.mkdir();
        }
    }

}
