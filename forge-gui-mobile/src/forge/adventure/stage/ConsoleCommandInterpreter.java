package forge.adventure.stage;


import forge.StaticData;
import forge.adventure.pointofintrest.PointOfInterest;
import forge.adventure.util.Current;
import forge.item.PaperCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleCommandInterpreter {
    public String complete(String text) {
            String[] words=splitOnSpace(text);

        Command currentCommand=root;
        String completionString="";
        int i;
        for(i =0;i<words.length;i++)
        {
            String name=words[i];
            if(!currentCommand.children.containsKey(name))
            {
                for(String key:currentCommand.children.keySet())
                {
                    if(key.startsWith(name))
                    {
                        return completionString+key+" ";
                    }
                }
                break;
            }
            completionString+=name+" ";
            currentCommand=currentCommand.children.get(name);
        }
        return text;
    }

    private String[] splitOnSpace(String text) {
        List<String> matchList = new ArrayList<String>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(text);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                matchList.add(regexMatcher.group(2));
            } else {
                matchList.add(regexMatcher.group());
            }
        }
        return matchList.toArray(new String[0]);
    }

    class Command
    {
        HashMap<String,Command> children=new HashMap<>();
        Function<String[],String> function;
    }
     String command(String text)
    {
        String[] words=splitOnSpace(text);

        Command currentCommand=root;
        int i;
        for(i =0;i<words.length;i++)
        {
            String name=words[i];
            if(!currentCommand.children.containsKey(name))
                break;
            currentCommand=currentCommand.children.get(name);
        }
        if(currentCommand.function==null)
        {
            return "Command not found options are "+String.join(" ",Arrays.copyOfRange(words, 0, i))+"\n"+String.join("\n",currentCommand.children.keySet());
        }
        String[] parameters=Arrays.copyOfRange(words, i, words.length);
        for(int j=0;j<parameters.length;j++)
            parameters[j]=parameters[j].replaceAll("[\"']","");
        return currentCommand.function.apply(parameters);
    }
    Command root=new Command();
    void registerCommand(String[] path,Function<String[],String> function)
    {
        if(path.length==0)
            return;

        Command currentCommand=root;
        for(String name:path)
        {
            if(!currentCommand.children.containsKey(name))
                currentCommand.children.put(name,new Command());
            currentCommand=currentCommand.children.get(name);
        }
        currentCommand.function=function;
    }
    public    ConsoleCommandInterpreter()
    {
        registerCommand(new String[]{"teleport", "to"}, s -> {
            if(s.length<2)
                return "Command needs 2 parameter";
            WorldStage.getInstance().GetPlayer().setPosition(Integer.parseInt(s[0]),Integer.parseInt(s[1]));

            return  "teleport to ("+s[0]+","+s[1]+")";
        });
        registerCommand(new String[]{"teleport", "to", "poi"}, s -> {
            if(s.length<1)
                return "Command needs 1 parameter";
            PointOfInterest poi=Current.world().findPointsOfInterest(s[0]);
            if(poi==null)
                return "poi "+s[0]+" not found";
            WorldStage.getInstance().GetPlayer().setPosition(poi.getPosition());
            return  "teleport to "+s[0]+"("+poi.getPosition()+")";
        });
        registerCommand(new String[]{"give", "gold"}, s -> {
            if(s.length<1)
                return "Command needs 1 parameter";
            int amount=0;
            try {
                amount=Integer.parseInt(s[0]);
            }
            catch (Exception e)
            {
                return "Can not convert "+s[0]+" to number";
            }
            Current.player().giveGold(amount);
            return "Added "+amount+" gold";
        });
        registerCommand(new String[]{"give", "life"}, s -> {
            if(s.length<1)
                return "Command needs 1 parameter";
            int amount=0;
            try {
                amount=Integer.parseInt(s[0]);
            }
            catch (Exception e)
            {
                return "Can not convert "+s[0]+" to number";
            }
            Current.player().addMaxLife(amount);
            return "Added "+amount+" max life";
        });
        registerCommand(new String[]{"give", "card"}, s -> {
            if(s.length<1)
                return "Command needs 1 parameter";
            PaperCard card=StaticData.instance().getCommonCards().getCard(s[0]);
            if(card==null)
                return "can not find card "+s[0];
            Current.player().addCard(card);
            return "Added card "+s[0];
        });
        registerCommand(new String[]{"give", "item"}, s -> {
            if(s.length<1)
                return "Command needs 1 parameter";
            if(Current.player().addItem(s[0]))
                return "Added item "+s[0];
            return "can not find item "+s[0];
        });
        registerCommand(new String[]{"heal"}, s -> {
            Current.player().heal();
            return "Player life back to "+Current.player().getLife();
        });
        registerCommand(new String[]{"debug","on"}, s -> {
            Current.setDebug(true);
            return "Debug mode on";
        });
        registerCommand(new String[]{"debug","off"}, s -> {
            Current.setDebug(false);
            return "Debug mode off";
        });
        registerCommand(new String[]{"remove","enemy","all"}, s -> {
            if(!MapStage.getInstance().isInMap())
            {
                return "Only supported for poi";
            }
            MapStage.getInstance().removeAllEnemies();
            return "removed all enemies";
        });
        registerCommand(new String[]{"remove","enemy"}, s -> {
            if(s.length<1)
                return "Command needs 1 parameter";
            int id=0;
            try {
                id=Integer.parseInt(s[0]);
            }
            catch (Exception e)
            {
                return "Can not convert "+s[0]+" to number";
            }
            if(!MapStage.getInstance().isInMap())
            {
                return "Only supported for poi";
            }
            MapStage.getInstance().deleteObject(id);
            return "removed enemy "+s[0];
        });
    }
}