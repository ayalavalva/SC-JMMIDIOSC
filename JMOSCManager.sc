JMOSCManager { // A class designed to handle OSC (Open Sound Control) communication, enabling the sending of OSC messages from SuperCollider to other OSC-enabled devices or software.
    classvar <sharedInstance; // A class variable that holds a shared instance of the JMOSCManager, ensuring a singleton pattern for OSC communication management.
    var <>oscServAddr, <>oscServPort; // Instance variables for the OSC server's address and port, which define where OSC messages will be sent.
    var <>oscAddr; // An OSC client instance used for sending OSC messages.

    // Constructor: Initializes a new instance of JMOSCManager with a specified OSC server address and port.
    *new { |oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort);
    }

    // Initialization method: Configures the OSC client with the provided server address and port.
    init { |oscServAddr, oscServPort|
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort); // Initialize the OSC client with the server address and port.
    }

    // Class method to obtain a shared instance of the JMOSCManager. If no instance exists, one is created. This ensures that only one instance is used throughout the application.
    *getSharedInstance { |oscServAddr = "127.0.0.1", oscServPort = 9000|
        if(sharedInstance.isNil, { // Check if the sharedInstance is not initialized.
            sharedInstance = this.new(oscServAddr, oscServPort); // Create a new instance with default or provided parameters.
        });
        ^sharedInstance; // Return the sharedInstance.
    }

    // Method to send an OSC message to the configured address and port. It constructs the OSC message with the provided path and arguments.
    send { |oscPath, args, debug = true|
        if (debug) {"Sending OSC Message to Address: % Value: %".format(oscPath, args).postln}; // Log the outgoing OSC message for debugging.
        this.oscAddr.sendMsg(oscPath, *args); // Use the OSC client to send the message.
    }
}