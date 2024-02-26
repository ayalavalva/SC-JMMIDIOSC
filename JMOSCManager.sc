JMOSCManager {
    var <>oscServAddr, <>oscServPort;
    var <>oscAddr;
    var <>oscPath;

    *new {|oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort)
    }

    init { |oscServAddr, oscServPort|
        oscServAddr = this.oscServAddr;
        oscServPort = this.oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort);
    }

    sendToOSC { |oscPath, args|
        oscAddr.sendMsg(oscPath, *args);
    }
}