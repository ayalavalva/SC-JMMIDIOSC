JMOSCManager { // A class designed to handle OSC (Open Sound Control) communication, enabling the sending of OSC messages from SuperCollider to other OSC-enabled devices or software.
    classvar <sharedInstance; // A class variable that holds a shared instance of the JMOSCManager, ensuring a singleton pattern for OSC communication management.
    var <>oscServAddr, <>oscServPort; // Instance variables for the OSC server's address and port, which define where OSC messages will be sent.
    var <>oscAddr; // An OSC client instance used for sending OSC messages.
    var <oscServerStatus = false; // A flag to indicate whether the OSC server is currently running.

    // Constructor: Initializes a new instance of JMOSCManager with a specified OSC server address and port.
    *new { |oscServAddr, oscServPort|
        ^super.new.init(oscServAddr, oscServPort);
    }

    // Initialization method: Configures the OSC client with the provided server address and port.
    init { |oscServAddr, oscServPort|
        this.oscServAddr = oscServAddr;
        this.oscServPort = oscServPort;
        this.oscAddr = NetAddr.new(oscServAddr, oscServPort); // Initialize the OSC client with the server address and port.
        this.checkOSCServerStatus; // Check the status of the Open Stage Control server.
    }

    // Method to check the status of the OSC server by sending a 'ping' message and waiting for a 'pong' response.
    checkOSCServerStatus {
        // Send a 'ping' message to the server
        oscAddr.sendMsg('/ping');
        "Sending ping message to Open Stage Control server.".postln;

        // Listen for a 'pong' response to determine if the server is up
        OSCdef(\pongResponse, { |msg|
            oscServerStatus = true;  // Server responded, mark as up
            "Open Stage Control server is up.".postln;
            OSCdef(\pongResponse).free;  // Clean up after receiving response
        }, '/pong', oscAddr);

        // Set a timeout for waiting for the response
        (1.0).wait;  // Adjust the timeout as needed

        // After the timeout, if no response has been received, assume the server is down
        if (oscServerStatus.not) {
            "Open Stage Control server did not respond. Won't send any OSC messages.".warn;
            OSCdef(\pongResponse).free;  // Ensure cleanup even without response
        }
    }

    // Class method to obtain a shared instance of the JMOSCManager. If no instance exists, one is created. This ensures that only one instance is used throughout the application.
    *getSharedInstance { |oscServAddr = "127.0.0.1", oscServPort = 9000| // Default Open Stage Control OSC server IP address and port
        if(sharedInstance.isNil, { // Check if the sharedInstance is not initialized.
            sharedInstance = this.new(oscServAddr, oscServPort); // Create a new instance with default or provided parameters.
        });
        ^sharedInstance; // Return the sharedInstance.
    }

    // Method to send an OSC message to the configured address and port. It constructs the OSC message with the provided path and arguments.
    send { |oscPath, args, debug = true|
        if(oscServerStatus) { 
            if (debug) {"Sending OSC Message to Address: % Value: %".format(oscPath, args).postln}; // Log the outgoing OSC message for debugging.
            this.oscAddr.sendMsg(oscPath, *args); // Use the OSC client to send the message.
        }
    }
}