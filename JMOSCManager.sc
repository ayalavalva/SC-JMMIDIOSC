JMOSCManager {
    var <>oscServAddr, <>oscServPort;
    var <>oscAddr;
    // Declare sharedInstance as a class variable
    classvar <sharedInstance;

    *new { |oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort);
    }

    init { |oscServAddr, oscServPort|
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort);
    }

    // Class method to get or create the shared instance
    *getSharedInstance { |oscServAddr = "127.0.0.1", oscServPort = 9000|
        if(sharedInstance.isNil, {
            sharedInstance = this.new(oscServAddr, oscServPort);
        });
        ^sharedInstance;
    }

    send { |oscPath, args|
    "Sending OSC Message: %, Args: %".format(oscPath, args).postln;
        this.oscAddr.sendMsg(oscPath, *args);
    }
}