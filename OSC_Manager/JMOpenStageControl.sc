/*
classvar <sharedInstance;
var rootDict;
var level1Dict;
var level2Dict;



    *new { |oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort);
    }

    init { |oscServAddr, oscServPort|
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort); // Initialize the OSC client with the server address and port.
        this.checkOSCServerStatus; // Check the status of the Open Stage Control server.

        // Initialize the dictionaries.
        rootDict = Dictionary.new;
        level1Dict = Dictionary.new;
        level2Dict = Dictionary.new;

        this.initRootDict


        initRootDict { // Initialize the root dictionary.
            rootDict = Dictionary.new;
            rootDict.put("type", "root");
            rootDict.put("name", "root");
            rootDict.put("children", level1Dict);
        }

        initLevel1Dict { // Initialize the level 1 dictionary.
            level1Dict = Dictionary.new;
            level1Dict.put("type", "level1");
            level1Dict.put("name", "level1");
            level1Dict.put("children", level2Dict);
        }

        initLevel2Dict { // Initialize the level 2 dictionary.
            level2Dict = Dictionary.new;
            level2Dict.put("type", "level2");
            level2Dict.put("name", "level2");
        }