JMIntechPO16 : JMIntechControllers { // JMIntechPO16: A subclass of JMIntechControllers designed specifically for the "Intech Studio PO16" MIDI controller, which features 16 potentiometers
    classvar <>classDeviceNumb = 0; // A class variable to keep track of the number of PO16 device instances
    var <>startCC; // Instance variables for the starting MIDI CC (Control Change) number 
    var <>deviceNumb; // Instance variables for the unique device number assigned to each instance

    // Constructor: Creates a new instance of JMIntechPO16 with optional parameters for MIDI channel, starting CC number, OSC server address, and port
    *new { |midiChannel=0, startCC=0, oscServAddr="127.0.0.1", oscServPort=9000|
        this.classDeviceNumb = this.classDeviceNumb + 1; // Increment the classDeviceNumb to assign a unique number to this instance
        ^super.new.init("Intech Studio PO16", "PO16", midiChannel, oscServAddr, oscServPort).initPO16(startCC) // Call the superclass's init method to set up the device with its name, short name, MIDI channel, OSC server address, and port
    }

    // initPO16: Initializes the PO16 device settings, specifically setting up the 16 potentiometers
    initPO16 { |startCC|
        this.deviceNumb = classDeviceNumb; // Assign the unique device number from classDeviceNumb
        this.startCC = startCC; // Set the starting MIDI CC number for the potentiometers
        this.potCount = 16; // Specify that this device has 16 potentiometers
        super.initializeMIDIElements; // Call the superclass's initializeMIDIElements method to set up the MIDI elements (potentiometers) for this device
    }
}