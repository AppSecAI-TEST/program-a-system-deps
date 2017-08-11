package com.jyo.sf.main;

import java.util.Collections;
import java.util.List;

import com.jyo.sf.enums.CommandType;

public class Command {

    private final CommandType commandType;
    private final List<String> args;
    
    public Command(CommandType commandType,List<String> args){
        this.commandType = commandType;
        this.args = Collections.unmodifiableList(args);
    }
    
    public CommandType getCommandType() {
        return commandType;
    }

    public List<String> getArgs() {
        return args;
    }
    
}
