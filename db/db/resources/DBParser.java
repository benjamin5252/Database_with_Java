import java.util.*;
import java.io.*;

import DBExceptions.*;

public class DBParser {

    ArrayList<String> commands;
    DBModel model;
    int index = 0;

    //a class responsible for parsing commands
    public DBParser(DBModel Model){
        this.model = Model;
    }

    public void parseCommands(ArrayList<String> inputCommands) throws DBException{
        this.commands = inputCommands;
        try{
            CommandType();
        }catch (DBException e){
            throw e;
        }

    }
    //parse the grammar of commandType
    private void CommandType() throws DBException {
        if(commands.get(index).equalsIgnoreCase(";")){
            commands.clear();
            return;
        }
        try{
            if(commands.get(index).equalsIgnoreCase("USE")){
                Use();
            }else if(commands.get(index).equalsIgnoreCase("CREATE")){
                Create();
            }else if(commands.get(index).equalsIgnoreCase("DROP")){
                Drop();
            }else if(commands.get(index).equalsIgnoreCase("ALTER")){
                Alter();
            }else if(commands.get(index).equalsIgnoreCase("INSERT")){
                Insert();
            }else if(commands.get(index).equalsIgnoreCase("SELECT")){
                Select();
            }else if(commands.get(index).equalsIgnoreCase("UPDATE")){
                Update();
            }else if(commands.get(index).equalsIgnoreCase("DELETE")){
                Delete();
            }else if(commands.get(index).equalsIgnoreCase("JOIN")){
                Join();
            }else {
                throw new DBParseException("Invalid query");
            }
        }catch (DBException e){
            throw e;
        }

        index ++;
        try{
            CommandType();
        }catch (DBParseException e){
            throw e;
        }

    }

    //parse the USE grammar and save the database used into DBModel.databaseUsed
    private void Use() throws DBException {
        index ++;
        DBFileIo DBIo = new DBFileIo();
        if(DBIo.hasDatabase(DatabaseName())){
            model.databaseUsed = DatabaseName();
        }else{
            throw new DBIoException("Unknown database");
        }
    }

    //a method to read the DatabaseName in commands
    private String DatabaseName() throws DBParseException {
        if(!(isCommandType(commands.get(index)))){
            return commands.get(index);
        }else {
            throw new DBParseException("Not a valid DatabaseName");
        }

    }

    //a method to read the TableName in grammar
    private String TableName() throws DBParseException {
        if(!(isCommandType(commands.get(index)))){
            return commands.get(index);
        }else {
            throw new DBParseException("Not a valid TableName");
        }
    }

    //parse Create in grammar
    private void Create() throws DBException {
        index ++;
        try{
            if(commands.get(index).equalsIgnoreCase("DATABASE")){
                CreateDatabase();
            }else if(commands.get(index).equalsIgnoreCase("TABLE")){
                CreateTable();
            }else{
                throw new DBParseException("Create a unknown type");
            }

        }catch (DBException e){
            throw e;
        }
    }

    //create a new Database, and store it with DBFileIo.addDatabase
    private void CreateDatabase() throws DBException {
        index ++;
        String DBName = DatabaseName();
        DBFileIo DBIo = new DBFileIo();
        DBIo.addDatabase(DBName);
    }

    //create a new Table in the database used, and store it with DBFileIo.addTable
    private void CreateTable() throws DBException {
        index ++;
        ArrayList<String> colArray = new ArrayList<String>();
        String TBName = TableName();
        DBTable tab = new DBTable(TBName);
        tab.addColumn("id");

        if(commands.get(index + 1).equals("(")){
            index ++;
            index ++;
            //passing a ArrayList colArray into AttributeList() to take the AttributeList parsed
            AttributeList(colArray);
            for(String i : colArray){
                tab.addColumn(i);
            }
        }
        DBFileIo io = new DBFileIo();
        try{
            io.addTable(model.databaseUsed, tab);
        }catch (DBException e){
            throw e;
        }
    }

    //parse AttributeList and update the input ArrayList<String> colArray
    private void AttributeList(ArrayList<String> colArray) throws DBException{
        String strTemp = "";
        if(commands.get(index).equals(")")){
            return;
        }
        strTemp = AttributeName();
        colArray.add(strTemp);
        index ++;
        if(commands.get(index).equals(")")){
            return;
        }else if(commands.get(index).equals(",")){

        }else{
            throw new DBParseException("Invalid query");
        }
        index ++;
        AttributeList(colArray);
    }

    private void Drop() throws DBException {
        index ++;
        if (commands.get(index).equalsIgnoreCase("DATABASE")){
            DropDatabase();
        }else if (commands.get(index).equalsIgnoreCase("TABLE")){
            DropTable();
        }
    }

    //parse and perform drop database with DBFileIo.dropDatabase
    private void DropDatabase() throws DBException {
        index++;
        String DBName = DatabaseName();
        DBFileIo io = new DBFileIo();
        io.dropDatabase(DBName);
    }

    //parse and perform drop table with DBFileIo.dropTable
    private void DropTable() throws DBException {
        index++;
        String TBName = TableName();
        DBFileIo io = new DBFileIo();
        try{
            io.dropTable(model.databaseUsed, TBName);
        }catch (DBException e){
            throw e;
        }

    }

    //parse Alter and perform a method to add or drop the column of the table
    private void Alter() throws DBException {
        index ++;
        if(!commands.get(index).equalsIgnoreCase("TABLE")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        String tableName;
        tableName = TableName();
        index ++;
        String alterationType;
        alterationType = AlterationType();
        index ++;
        String attributeName;
        attributeName = AttributeName();

        DBFileIo io = new DBFileIo();
        DBTable table = io.loadTable(model.databaseUsed, tableName);
        if(alterationType.equalsIgnoreCase("ADD")){
            if(table.column.contains(attributeName)){
                throw new DBParseException("The attribute already included");
            }else{
                table.addColumn(attributeName);
            }
        }else if(alterationType.equalsIgnoreCase("DROP")){
            if(!table.column.contains(attributeName)){
                throw new DBParseException("Attribute does not exist");
            }else{
                table.dropColumn(attributeName);
            }
        }
        io.updateTable(model.databaseUsed, table);
    }

    //parse and get the AlterationType and return it as string
    private String AlterationType() throws DBException{
        if(     commands.get(index).equalsIgnoreCase("ADD")
            ||  commands.get(index).equalsIgnoreCase("DROP")){
            return commands.get(index);
        }else{
            throw new DBParseException("Invalid query");
        }
    }

    //parse and perform Insert
    private void Insert() throws DBException {
        String TBName;
        index ++;
        if(!commands.get(index).equalsIgnoreCase("INTO")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        //take the table name parsed from commands
        TBName = TableName();
        index ++;
        if(!commands.get(index).equalsIgnoreCase("VALUES")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        if(!commands.get(index).equalsIgnoreCase("(")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        DBFileIo io = new DBFileIo();
        //load the table to insert into
        DBTable tab = io.loadTable(model.databaseUsed, TBName);
        tab.addData("" + (tab.data.size() + 1));
        //pass table into ValueList() to take the update from valueList
        ValueList(tab);
        //update the table in disk with DBFileIo.updateTable
        io.updateTable(model.databaseUsed, tab);
    }

    //parse and perform ValueList, taking a table to update
    private void ValueList(DBTable table) throws DBException{
        String strTemp = "";
        if(commands.get(index).equals(")")){
            return;
        }
        strTemp = Value();
        table.addData(strTemp);
        index ++;
        if(commands.get(index).equals(")")){
            return;
        }else if(commands.get(index).equals(",")){

        }else{
            throw new DBParseException("Wrong grammar for valueList");
        }
        index ++;
        ValueList(table);
    }

    private void Select() throws DBException {
        ArrayList<String> ColumnToSelect = new ArrayList<String>();
        String TBName;
        DBTable tab;
        index ++;
        //pass ArrayList<String> ColumnToSelect into WildAttribList() to get the result of WildAttribList
        WildAttribList(ColumnToSelect);
        index ++;
        TBName = TableName();
        DBFileIo io = new DBFileIo();
        //load out the table selected
        tab = io.loadTable(model.databaseUsed, TBName);
        //check the columns selected is in the table or not
        for(String i : ColumnToSelect){
            if( !tab.column.contains(i) && !ColumnToSelect.get(0).equals("*") ){
                throw new DBParseException("Attribute does not exist");
            }
        }
        //create a boolean[] to record the corresponding row to select
        //initialize it into all true
        boolean[] rowChosen = new boolean[tab.data.size()];
        Arrays.fill(rowChosen, true);
        //if next command to parse is "where",  lead to Condition
        if(commands.get(index + 1).equalsIgnoreCase("WHERE")){
            index ++;
            index ++;
            //take the output of condition with boolean[] rowChosen
            rowChosen = Condition(tab);
        }
        //check the ending is ; or not, after all of the parsing of select
        if(!commands.get(index + 1).equals(";")){
            throw new DBParseException("Invalid query");
        }

        //before updating the model.outputData, make sure that it is clear
        model.outputData.clear();

        //if the WildAttribList parsed into *, choose all columns of the table
        if(ColumnToSelect.get(0).equals("*")){
            model.outputData.add(tab.column);
            for(int j = 0;j < tab.data.size(); j++){
                if(rowChosen[j] == true){
                    model.outputData.add(tab.data.get(j));
                }
            }
        }else{
            //update the columns in model.outputData according to the WildAttribList
            model.outputData.add(new ArrayList<String>());
            for(int i = 0; i < tab.column.size(); i++){
                if(ColumnToSelect.contains(tab.column.get(i))){
                    model.outputData.get(0).add(tab.column.get(i));
                }
            }
            //update the data in model.outputData according to the WildAttribList and Condition
            for(int j = 0; j < tab.data.size(); j++){
                if(rowChosen[j] == true){
                    ArrayList<String> dataRow = new ArrayList<String>();
                    for(int i = 0; i < tab.column.size(); i++){
                        if(ColumnToSelect.contains(tab.column.get(i))){
                            dataRow.add(tab.data.get(j).get(i));
                        }
                    }
                    model.outputData.add(dataRow);
                }
            }
        }

    }

    //parse and take out AttributeName in commands
    private String AttributeName(){
        ArrayList<String> strArrTemp = new ArrayList<String>();
        String strTemp = "";
        //combine the commands enclosed by " or ' into one String
        if(commands.get(index).equals("\"") || commands.get(index).equals("\'")){
            while ( commands.get(index).equals("\"") || commands.get(index).equals("\'")){
                index ++;
            }
            while ( !commands.get(index).equals("\"") && !commands.get(index).equals("\'")){
                strArrTemp.add(commands.get(index));
                index ++;
            }
            strTemp = "" + strArrTemp.get(0);
            for(int i = 1; i < strArrTemp.size(); i++){
                //store the combined String with " in between, in convenience for further processing
                strTemp = strTemp + "\"" + strArrTemp.get(i);
            }
            return  strTemp;
        }else{
            return commands.get(index);
        }

    }

    //parse and take out Value in commands
    private String Value() throws DBException{
        ArrayList<String> strArrTemp = new ArrayList<String>();
        String strTemp = "";
        //combine the commands enclosed by " or ' into one String
        if(commands.get(index).equals("\"") || commands.get(index).equals("\'")){
            while ( commands.get(index).equals("\"") || commands.get(index).equals("\'")){
                index ++;
            }
            while ( !commands.get(index).equals("\"") && !commands.get(index).equals("\'")){
                strArrTemp.add(commands.get(index));
                index ++;
                if(commands.get(index).equals(";")){
                    throw new DBParseException("Invalid query");
                }
            }
            strTemp = "" + strArrTemp.get(0);
            for(int i = 1; i < strArrTemp.size(); i++){
                //store the combined String with " in between, in convenience for further processing
                strTemp = strTemp + "\"" + strArrTemp.get(i);
            }
            return  strTemp;
        }else{
            return commands.get(index);
        }

    }

    //parse Condition and return a boolean[]
    //containing whether the corresponding index of rows of data is chosen or not
    private boolean[] Condition(DBTable tab) throws DBException{
        String attribute;
        String operator;
        String booleanOperator;
        String value;
        //boolean[] isChosen to record the row to choose
        boolean[] isChosen = new boolean[tab.data.size()];
        //isChosen1 and isChosen2 prepared to take the returns of the sub Conditions
        boolean[] isChosen1;
        boolean[] isChosen2;
        //initialize isChosen with all false
        Arrays.fill(isChosen, false);
        //if there is no (, there is no sub conditions to parse
        //hence, the condition can be returned
        if(!commands.get(index).equals("(")){
            attribute = AttributeName();
            index ++;
            operator = Operator();
            index ++;
            value = Value();

            if(!tab.column.contains(attribute)){
                throw new DBParseException("Attribute not exist");
            }
            int colNum = tab.column.indexOf(attribute);
            //If the operator is equal to ==, check the data is equal to value in String or not.
            //Than, update the isChosen
            if(operator.equals("==")){
                for(int j = 0; j < tab.data.size(); j++){
                    if(tab.data.get(j).get(colNum).equals(value)){
                        isChosen[j] = true;
                    }
                }

            //If the operator is equal to >, convert the data into Float
            //If the data is greater than Value, update the corresponding isChosen into true
            }else if(operator.equals(">")){
                for(int j = 0; j < tab.data.size(); j++){
                    if(!isNumber(tab.data.get(j).get(colNum))){
                        throw new DBParseException("Attribute cannot be converted to number");
                    }
                    if(!isNumber(value)){
                        throw new DBParseException("Value cannot be converted to number");
                    }
                    if( Double.parseDouble(tab.data.get(j).get(colNum)) >  Double.parseDouble(value)){
                        isChosen[j] = true;
                    }
                }

            //If the operator is equal to <, convert the data into Float
            //If the data is less than Value, update the corresponding isChosen into true
            }else if(operator.equals("<")){
                for(int j = 0; j < tab.data.size(); j++){
                    if(!isNumber(tab.data.get(j).get(colNum))){
                        throw new DBParseException("Attribute cannot be converted to number");
                    }
                    if(!isNumber(value)){
                        throw new DBParseException("Value cannot be converted to number");
                    }
                    if( Double.parseDouble(tab.data.get(j).get(colNum)) <  Double.parseDouble(value)){
                        isChosen[j] = true;
                    }
                }

            //If the operator is equal to >=, convert the data into Float
            //If the data is greater than and equal to Value, update the corresponding isChosen into true
            }else if(operator.equals(">=")){
                for(int j = 0; j < tab.data.size(); j++){
                    if(!isNumber(tab.data.get(j).get(colNum))){
                        throw new DBParseException("Attribute cannot be converted to number");
                    }
                    if(!isNumber(value)){
                        throw new DBParseException("Value cannot be converted to number");
                    }
                    if(     Double.parseDouble(tab.data.get(j).get(colNum)) >  Double.parseDouble(value)
                        ||  doubleEqual(Double.parseDouble(tab.data.get(j).get(colNum)), Double.parseDouble(value))){
                        isChosen[j] = true;
                    }
                }

            //If the operator is equal to <=, convert the data into Float
            //If the data is less than and equal to Value, update the corresponding isChosen into true
            }else if(operator.equals("<=")){
                for(int j = 0; j < tab.data.size(); j++){
                    if(!isNumber(tab.data.get(j).get(colNum))){
                        throw new DBParseException("Attribute cannot be converted to number");
                    }
                    if(!isNumber(value)){
                        throw new DBParseException("Value cannot be converted to number");
                    }
                    if(     Double.parseDouble(tab.data.get(j).get(colNum)) <  Double.parseDouble(value)
                        ||  doubleEqual(Double.parseDouble(tab.data.get(j).get(colNum)), Double.parseDouble(value))){
                        isChosen[j] = true;
                    }
                }

            //If the operator is equal to !=, compare data and value in String
            //If the data is not equal to Value, update the corresponding isChosen into true
            }else if(operator.equals("!=")){

                for(int j = 0; j < tab.data.size(); j++){
                    if(!tab.data.get(j).get(colNum).equals(value)){
                        isChosen[j] = true;
                    }
                }
            //If the operator is equal to "LIKE", compare data and value in String
            //if the data contain the value in substring than set the isChosen into true
            }else if(operator.equalsIgnoreCase("LIKE")){
                for(int j = 0; j < tab.data.size(); j++){
                    if(isNumber(tab.data.get(j).get(colNum)) || isNumber(value)){
                        throw new DBParseException("String expected");
                    }
                    if(tab.data.get(j).get(colNum).contains(value)){
                        isChosen[j] = true;
                    }
                }
            } else{
                throw new DBParseException("The condition operator is wrong");
            }
            return isChosen;
        //solve the nested Conditions
        }else{
            index ++;
            isChosen1 = Condition(tab);
            index++;
            if(!commands.get(index).equals(")")){
                throw new DBParseException("Require a )");
            }
            index ++;
            booleanOperator = BooleanOperator();
            index ++;
            if(!commands.get(index).equals("(")){
                throw new DBParseException("Require a )");
            }
            index ++;
            isChosen2 = Condition(tab);
            index++;
            if(!commands.get(index).equals(")")){
                throw new DBParseException("Require a )");
            }
            //update the isChosen after the boolean operation of two sub conditions
            if(booleanOperator.equalsIgnoreCase("AND")){
                for(int i = 0; i < isChosen.length; i++){
                    isChosen[i] = isChosen1[i] && isChosen2[i];
                }
            }else if(booleanOperator.equalsIgnoreCase("OR")){
                for(int i = 0; i < isChosen.length; i++){
                    isChosen[i] = isChosen1[i] || isChosen2[i];
                }
            }
            return isChosen;
        }

    }

    //parse and extract the Operator
    private String Operator() throws DBException{
        String operation = "";
        if(     commands.get(index).equals("=") || commands.get(index).equals(">") || commands.get(index).equals("<")
            ||  commands.get(index).equals("!") || commands.get(index).equalsIgnoreCase("LIKE")){
            operation = operation + commands.get(index);
            if(commands.get(index + 1).equals("=")){
                index ++;
                operation = operation + "=";
            }
            if(     operation.equals("==") || operation.equals(">") || operation.equals("<")
                ||  operation.equals(">=") || operation.equals("<=") || operation.equals("!=")
                ||  operation.equalsIgnoreCase("LIKE")){
                return operation;
            }else{
                throw new DBParseException("Require a right operator1");
            }
        }else{
            throw new DBParseException("Require a right operator2" + commands.get(index - 1));
        }
    }

    //extract the boolean operator
    private  String BooleanOperator() throws DBException{
        if(commands.get(index).equalsIgnoreCase("AND") ||commands.get(index).equalsIgnoreCase("OR")){
            return commands.get(index);
        }else{
            throw new DBParseException("Require a right booleanOperator");
        }
    }

    //parse WildAttribList, taking ArrayList ColumnToSelect to update with the column chosen
    private void WildAttribList(ArrayList<String> ColumnToSelect) throws DBException{
        ColumnToSelect.add(AttributeName());
        if(commands.get(index + 1).equalsIgnoreCase("FROM")){
            index ++;
            return;
        }else if(commands.get(index + 1).equalsIgnoreCase(",")){
            index ++;
            index ++;
            WildAttribList(ColumnToSelect);
        }else{
            throw new DBParseException("Invalid query");
        }
    }

    //parse and perform Update
    private void Update() throws DBException {
        index ++;
        DBFileIo io = new DBFileIo();
        if(model.databaseUsed == null){
            throw new DBIoException("No database used");
        }
        //load the table data from file according to the TableName parsed
        DBTable table = io.loadTable(model.databaseUsed, TableName());
        index++;
        if(!commands.get(index).equalsIgnoreCase("SET")){
            throw new DBParseException("Invalid query");
        }
        index++;
        //create a String[] to record the data to update to the corresponding columns
        String[] updateList = new String[table.column.size()];
        //while parsing the NameValueList, updateList is updated
        NameValueList(updateList, table);
        index ++;
        if(!commands.get(index).equalsIgnoreCase("WHERE")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        //create a boolean[] to record the corresponding row to select
        boolean[] rowChosen = new boolean[table.data.size()];
        //while parsing the Condition(), a boolean array recording the row to choose is returned
        rowChosen = Condition(table);

        for (int j = 0; j < table.data.size(); j++) {
            if(rowChosen[j] == true){
                for(int i = 0; i < table.column.size(); i++){
                    if(updateList[i] != null){
                        table.data.get(j).set(i, updateList[i]);
                    }
                }
            }
        }
        io.updateTable(model.databaseUsed, table);
    }

    //parse NameValueList, and update the updateList
    //The updateList contain the data to update, with the array index corresponding to the column index
    private void NameValueList(String[] updateList, DBTable table) throws DBException{
        NameValuePair(updateList, table);
        if(commands.get(index + 1).equalsIgnoreCase("WHERE")){
            return;
        }
        index ++;
        if(!commands.get(index).equals(",")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        NameValueList(updateList, table);
    }

    //update the updateList every NameValuePair
    private void NameValuePair(String[] updateList, DBTable table) throws DBException{
        String name = AttributeName();
        if(!table.column.contains(name)){
            throw new DBParseException("Attribute does not exist");
        }
        index ++;
        if(!commands.get(index).equals("=")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        String value = Value();
        for(int i = 0; i < table.column.size(); i++){
            if(table.column.get(i).equals(name)){
                updateList[i] = value;
            }
        }
    }

    //parse and perform deleting the data row in the target table
    private void Delete() throws DBException {
        index ++;
        if(!commands.get(index).equalsIgnoreCase("FROM")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        DBFileIo io = new DBFileIo();
        if(model.databaseUsed == null){
            throw new DBIoException("No database used");
        }
        DBTable table = io.loadTable(model.databaseUsed, TableName());
        index ++;
        if(!commands.get(index).equalsIgnoreCase("WHERE")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        //create a boolean[] to record the corresponding row to select
        boolean[] rowChosen = new boolean[table.data.size()];
        rowChosen = Condition(table);

        //record the original data arrayList size, because it will change after remove()
        int originDataSize = table.data.size();
        //i index for removing object from data
        int i = 0;
        for(int j = 0; j < originDataSize; j++){
            if(rowChosen[j] == true){
                table.data.remove(i);
                //i-- because the table become smaller than previous
                i --;
            }
            i ++;
        }
        if(model.databaseUsed == null){
            throw new DBIoException("No database used");
        }
        io.updateTable(model.databaseUsed, table);
    }

    //parse Join, and perform a method to show data combing two tables
    private void Join() throws DBException {
        index ++;
        String tableName1 = TableName();
        index ++;
        if(!commands.get(index).equalsIgnoreCase("AND")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        String tableName2 = TableName();
        index ++;
        if(!commands.get(index).equalsIgnoreCase("ON")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        String attribute1 = AttributeName();
        index ++;
        if(!commands.get(index).equalsIgnoreCase("AND")){
            throw new DBParseException("Invalid query");
        }
        index ++;
        String attribute2 = AttributeName();

        DBFileIo io = new DBFileIo();
        DBTable table1 = io.loadTable(model.databaseUsed, tableName1);
        DBTable table2 = io.loadTable(model.databaseUsed, tableName2);
        //find the indexes of columns chosen to Join on
        int colNum1 = table1.column.indexOf(attribute1);
        int colNum2 = table2.column.indexOf(attribute2);
        model.outputData.clear();
        ArrayList<String> dataRow = new ArrayList<String>();
        //generate new columns for the Joined table
        dataRow.add("id");
        for(int i = 1; i < table1.column.size(); i++){
            dataRow.add(table1.name + "." + table1.column.get(i));
        }
        for(int i = 1; i < table2.column.size(); i++){
            dataRow.add(table2.name + "." + table2.column.get(i));
        }
        model.outputData.add(dataRow);
        //add the combined data into the table, and generate new id for the data
        int joinId = 1;
        for(int j = 0; j < table1.data.size(); j++){
            for(int i = 0; i < table2.data.size(); i++){
                if(table1.data.get(j).get(colNum1).equals( table2.data.get(i).get(colNum2) )){
                    ArrayList<String> row = new ArrayList<String>();
                    row.add("" + joinId);
                    for(int x = 1; x < table1.data.get(j).size(); x++){
                        row.add(table1.data.get(j).get(x));
                    }
                    for(int x = 1; x < table2.data.get(i).size(); x++){
                        row.add(table2.data.get(i).get(x));
                    }
                    model.outputData.add(row);
                    joinId ++;
                }
            }
        }
    }

    //a method to judge the input string is CommandType or not
    private boolean isCommandType(String input) {
        if(input.equalsIgnoreCase("USE") || input.equalsIgnoreCase("CREATE")
        || input.equalsIgnoreCase("DROP") || input.equalsIgnoreCase("ALTER")
        || input.equalsIgnoreCase("INSERT") || input.equalsIgnoreCase("SELECT")
        || input.equalsIgnoreCase("UPDATE") || input.equalsIgnoreCase("DELETE")
        || input.equalsIgnoreCase("JOIN") || input.equalsIgnoreCase("DATABASE")){
            return true;
        }
        return false;
    }

    //a method to check the two input doubles are equal or not
    private boolean doubleEqual(double d1, double d2){
        double epsilon = 0.000001d;
        if(Math.abs(d1 - d2) < epsilon){
            return true;
        }
        return false;
    }

    //check if the input string can be converted to number or not
    private boolean isNumber(String str){
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}

