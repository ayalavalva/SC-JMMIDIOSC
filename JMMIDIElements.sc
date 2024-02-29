JMMIDIElements {
    var <>deviceFullName, <>deviceShortName, <>deviceNumb, <>elementNumber, <>midiChannel;

    *new { |deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel|
        ^super.new(deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel)
    }

    init { |deviceFullName, deviceShortName, deviceNumb, elementNumber, midiChannel|
        this.deviceFullName = deviceFullName;
        this.deviceShortName = deviceShortName;
        this.deviceNumb = deviceNumb;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
    }
}