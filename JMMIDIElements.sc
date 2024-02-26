JMMIDIElements {
    var <>name, <>deviceNumb, <>elementNumber, <>midiChannel;

    *new { |name, deviceNumb, elementNumber, midiChannel|
        ^super.new(name, deviceNumb, elementNumber, midiChannel)
    }

    init { |name, deviceNumb, elementNumber, midiChannel|
        this.name = name;
        this.deviceNumb = deviceNumb;
        this.elementNumber = elementNumber;
        this.midiChannel = midiChannel;
    }
}