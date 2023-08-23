import DBExceptions.DBException;
import DBExceptions.DBParseException;

import java.util.*;
//a class to perform the data storage and data modification functions in a table
public class DBTable {
    public ArrayList<String> column;
    public ArrayList<ArrayList<String>> data;
    public String name;
    public String primaryKeyColumn = null;
    ArrayList<String> dataRow;
    int dataCount;

    public DBTable(String name) {
        this.column = new ArrayList<String>();
        this.data = new ArrayList<ArrayList<String>>();
        this.name = name;
        this.dataCount = 0;
        this.dataRow = new ArrayList<String>();
    }

    //A method to add String data into column
    public void addColumn(String column) {
        this.column.add(column);
    }

    //A method to append String into the content data of the table
    public void addData(String data) {
        dataRow.add(data);
        this.dataCount ++;
        if(this.dataCount >= this.column.size()){
            this.data.add(dataRow);
            this.dataRow = new ArrayList<String>();
        }
        this.dataCount %= this.column.size();
    }

    //A method to add whole row of data into the table
    public void addDataRow(ArrayList<String> dataRow){
        this.data.add(dataRow);
    }

    public void deleteDataRow(String primarykey) {
        for(int j = 0; j < data.size(); j++) {
            if(data.get(j).get(getColumn(primaryKeyColumn)).equals(primarykey)){
                data.remove(j);
            }
        }
    }

    //A method to get the column number of specific column name
    public int getColumn(String column) {
        try{
            for (int i = 0; i < this.column.size(); i++) {
                if(this.column.get(i).equals(column)){
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }


    //A method to drop a column of data
    public void dropColumn (String attribute) throws DBException {
        int index = -1;
        if(column.contains(attribute)){
            for(int i = 0; i < column.size(); i++){
                if(column.get(i).equals(attribute)){
                    index = i;
                }
            }
            if(index >= 0){
                for(ArrayList<String> i : data){
                    i.remove(index);
                }
                column.remove(index);
            }
        }else{
            throw new DBParseException("Attribute does not exist");
        }
    }
}
