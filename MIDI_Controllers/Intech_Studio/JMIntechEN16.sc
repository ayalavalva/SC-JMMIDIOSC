// JMIntechEN16: A subclass of JMIntechController designed specifically for the "Intech Studio EN16" MIDI controller, which features 16 encoders and 16 buttons

JMIntechEN16 : JMIntechController {
    classvar <>classDeviceNumb = 0; // Class variable to keep track of the number of EN16 device instances
    var <>startCC; // Starting MIDI CC (Control Change) number 
    var <>deviceNumb; // Unique device number assigned to each instance
    
    // Encoder instances
    var <>en1, <>en2, <>en3, <>en4;
    var <>en5, <>en6, <>en7, <>en8; 
    var <>en9, <>en10, <>en11, <>en12; 
    var <>en13, <>en14, <>en15, <>en16;
    // / Button instances
    var <>bu1, <>bu2, <>bu3, <>bu4;
    var <>bu5, <>bu6, <>bu7, <>bu8; 
    var <>bu9, <>bu10, <>bu11, <>bu12; 
    var <>bu13, <>bu14, <>bu15, <>bu16;

    var <>deviceOSCpath; // OSC path for the device

    *new { |midiChannel=0, startCC, deviceOSCpath="/en16", oscServAddr="127.0.0.1", oscServPort=9000, postMIDIOSC=false|
        this.classDeviceNumb = this.classDeviceNumb + 1; // Increment the classDeviceNumb to assign a unique number to this instance.
        
        ^super.new.init("Intech Studio EN16", "EN16", midiChannel, oscServAddr, oscServPort, postMIDIOSC).initEN16(startCC, deviceOSCpath)
    }

    initEN16 { |startCC, deviceOSCpath|
        this.deviceNumb = classDeviceNumb; // Assign the unique device number from classDeviceNumb
        this.startCC = startCC;
        this.deviceOSCpath = deviceOSCpath;

        // Assign 16 encoders instances in a loop
        (1..16).do { |i|
            var cc = this.startCC + (i - 1);
            var en = JMElementEncoder.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, i, this.midiChannel, this.deviceOSCpath, this.postMIDIOSC, cc);
            this.perform(("en" ++ i.asString ++ "_").asSymbol, en;);
        };

        // Assign 16 button instances in a loop
        (1..16).do { |i|
            var cc = this.startCC + 16 + (i - 1);
            var bu = JMElementButton.new(this, this.deviceFullName, this.deviceShortName, this.deviceNumb, i, this.midiChannel, this.deviceOSCpath, this.postMIDIOSC, cc);
            this.perform(("bu" ++ i.asString ++ "_").asSymbol, bu;);
        };
    }
}