package com.jyo.sf.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.jyo.sf.enums.CommandType;

@Component
public class CommandLineAppStartUpRunner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandLineAppStartUpRunner.class);
    private static final String DELIM = "  ";
    private static final String INSTALL = "Installing ";
    private static final String REMOVE = "Removing ";
    private static final String ALREADY_INSTALLED = " is already installed.";
    private static final String NOT_INSTALLED = " is not installed.";
    final List<String> components = new ArrayList<>();
    Map<String,List<String>> dependents = new HashMap<>();
    Map<String,List<String>> parents = new HashMap<>();
    List<String> installedExplicitly = new ArrayList<>();
    Scanner in= new Scanner(System.in);
    
    @Override
    public void run(String...args) throws Exception {
        logger.info("Application started "
                + "To kill this application, enter the word 'END' and press Enter.");
        
        while(true){
            String line = getInput();
            try{
                Command command = parseAndGetCommand(line); 

                if(!isValid(command)){
                    logger.warn("Unrecognized or bad command");
                    continue;
                }
                
                if(command.getCommandType()==CommandType.END)
                    break;

                process(command);

            }catch(Exception ex){
                logger.warn("Unrecognized or bad command",ex);
            }
            
        }
        
        if(in!=null)
            in.close();
        
    }
    
    private void process(Command command) {
        List<String> args = command.getArgs();
        
        switch(command.getCommandType()){
        case LIST:
            components.stream().forEach(i -> System.out.println(DELIM+i));
            break;
        case DEPEND:
            String component = args.get(0);
            List<String> depsForComponent = args.stream().skip(1).collect(Collectors.toList());
            dependents.put(component, depsForComponent);
            break;
        case INSTALL:
            component = args.get(0);
            if(components.contains(component)){
                System.out.println(DELIM+component+ALREADY_INSTALLED);
                break;
            }

            depsForComponent = getAllDependencies(component);
            for(String comp : depsForComponent){
                if(!components.contains(comp)){
                    System.out.println(DELIM+INSTALL+comp);
                    components.add(comp);    
                }
                
            }
            System.out.println(DELIM+INSTALL+component);
            components.add(component);
            installedExplicitly.add(component);
            break;
        case REMOVE:
            component = args.get(0);
            if(!components.contains(component)){
                System.out.println(DELIM+component+NOT_INSTALLED);
                break;
            }

            depsForComponent = getAllDependencies(component);
            for(String comp : depsForComponent){
                if(!installedExplicitly.contains(comp)){
                    System.out.println(DELIM+REMOVE+comp);
                    components.remove(comp);    
                }
                
            }
            System.out.println(DELIM+REMOVE+component);
            components.remove(component);
            installedExplicitly.remove(component);
            break;
        case END:
            
        }
    }

    private List<String> getAllDependencies(String component) {
        Queue<String> componentQ = new LinkedList<>();
        List<String> deps = new ArrayList<>();
        
        componentQ.add(component);
        while(!componentQ.isEmpty()){
            
            String comp = componentQ.poll();
            if(dependents.containsKey(comp)){
                List<String> depForComponent = dependents.get(comp);
                deps.addAll(depForComponent);
                componentQ.addAll(depForComponent);    
            }
        }
        
        return deps;
    }

    private Command parseAndGetCommand(String line) {
        if(line==null || line.trim().equals(""))
            throw new RuntimeException("No Input");
        
        String[] tokens = line.split("\\s+");
        CommandType commandType = CommandType.valueOf(tokens[0]);
        Command command = new Command(commandType,getArgs(tokens));
        return command;
    }

    private List<String> getArgs(String[] tokens) {
        if(tokens.length<=1)
            return new ArrayList<String>();
        
        return Arrays.stream(tokens).skip(1).collect(Collectors.toList());
    }

    private boolean isValid(Command command) {
        if(command.getCommandType()==CommandType.LIST && command.getArgs().size()>0)
            return false;
        if(command.getCommandType()==CommandType.END && command.getArgs().size()>0)
            return false;
        return true;
    }

    public String getInput(){
                return in.nextLine();
    }
}