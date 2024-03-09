JMIntechPBF4 : JMIntechControllers { // JMIntechPBF4: A subclass of JMIntechControllers designed specifically for the "Intech Studio PBF4" MIDI controller, which features 4 potentiometers, 4 faders and 4 buttons
    classvar <>classDeviceNumb = 0; // A class variable to keep track of the number of PBF4 device instances
    var <>startCC; // Instance variables for the starting MIDI CC (Control Change) number 
    var <>deviceNumb; // Instance variables for the unique device number assigned to each instance
    var <>deviceOSCpath; // Instance variables for the OSC path for the device

    // Constructor: Creates a new instance of JMIntechPO16 with optional parameters for MIDI channel, starting CC number, OSC server address, and port.
    *new { |midiChannel=0, startCC=64, deviceOSCpath="/pbf4", oscServAddr="127.0.0.1", oscServPort=9000|
        this.classDeviceNumb = this.classDeviceNumb + 1; // Increment the classDeviceNumb to assign a unique number to this instance.
        ^super.new.init("Intech Studio PBF4", "PBF4", midiChannel, oscServAddr, oscServPort).initPBF4(startCC, deviceOSCpath) // Call the superclass's init method to set up the device with its name, short name, MIDI channel, OSC server address, and port
    }

    // initPBF4: Initializes the PO16 device settings, specifically setting up the 4 potentiometers, 4 faders and 4 buttons
    initPBF4 { |startCC, deviceOSCpath|
        this.deviceNumb = classDeviceNumb; // Assign the unique device number from classDeviceNumb
        this.startCC = startCC; // Set the starting MIDI CC number for the potentiometers
        this.deviceOSCpath = deviceOSCpath ++ "_" ++ this.deviceNumb; // Set the OSC path for the device
        this.potCount = 4; // Specify that this device has 4 potentiometers
        this.fadCount = 4; // Specify that this device has 4 faders
        this.butCount = 4; // Specify that this device has 4 buttons
        super.initializeMIDIElements; // Call the superclass's initializeMIDIElements method to set up the MIDI elements (potentiometers, faders and buttons) for this device
    }
}