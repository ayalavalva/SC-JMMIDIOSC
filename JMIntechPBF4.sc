JMIntechPBF4 : JMIntechControllers {
    classvar <>classDeviceNumb = 0;
    var <>startCC;
    var <>deviceNumb;

    *new { |midiChannel=0, startCC=64, oscServAddr="127.0.0.1", oscServPort=9000|
        this.classDeviceNumb = this.classDeviceNumb + 1;
        ^super.new.init("Intech Studio PBF4", "PBF4", midiChannel, oscServAddr, oscServPort).initPBF4(startCC)
    }

    initPBF4 { |startCC|
        this.deviceNumb = classDeviceNumb;
        this.startCC = startCC;
        this.potCount = 4;
        this.fadCount = 4;
        this.butCount = 4;
        super.buildElementsDict;
    }
}