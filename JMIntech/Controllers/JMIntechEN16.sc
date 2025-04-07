JMIntechEN16 : JMIntechControllers { // JMIntechEN16: A subclass of JMIntechControllers designed specifically for the "Intech Studio EN16" MIDI controller, which features 16 encoders and 16 buttons
    classvar <>classDeviceNumb = 0; // A class variable to keep track of the number of EN16 device instances
    var <>startCC; // Instance variables for the starting MIDI CC (Control Change) number 
    var <>deviceNumb; // Instance variables for the unique device number assigned to each instance
    var <>deviceOSCpath; // Instance variables for the OSC path for the device
    var <>elementGroupOrder; // Instance variable with the order and the element count for each the element groups of the device

    // Constructor: Creates a new instance of JMIntechPO16 with optional parameters for MIDI channel, starting CC number, OSC server address, and port.
    *new { |midiChannel=0, startCC=32, deviceOSCpath="/en16", oscServAddr="127.0.0.1", oscServPort=9000, postMIDIOSC=false|
        this.classDeviceNumb = this.classDeviceNumb + 1; // Increment the classDeviceNumb to assign a unique number to this instance.
        ^super.new.init("Intech Studio EN16", "EN16", midiChannel, oscServAddr, oscServPort, postMIDIOSC).initEN16(startCC, deviceOSCpath) // Call the superclass's init method to set up the device with its name, short name, MIDI channel, OSC server address, and port
    }

    // initEN16: Initializes the PO16 device settings, specifically setting up the 16 encoders and 16 buttons
    initEN16 { |startCC, deviceOSCpath|
        this.deviceNumb = classDeviceNumb; // Assign the unique device number from classDeviceNumb
        this.startCC = startCC; // Set the starting MIDI CC number for the potentiometers
        this.deviceOSCpath = deviceOSCpath; // Set the OSC path for the device
        this.elementGroupOrder = [['EN', 16], ['BU', 16]]; // Set the element groups [type, count] and their order for the device
        super.initializeMIDIElements; // Call the superclass's initializeMIDIElements method to set up the MIDI elements (encoders and buttons) for this device
    }
}