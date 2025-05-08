JMDAWMIDIControllers {
    var <>postMIDIOSC;
    var <>po16, <>en16, <>pbf41, <>pbf42;

    *new { |postMIDIOSC|
        ^super.new.init(postMIDIOSC: postMIDIOSC);
    }
    
    init { |postMIDIOSC|
        this.postMIDIOSC = postMIDIOSC;

        this.po16 = JMIntechPO16(midiChannel: 0, startCC: 0, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);
        this.en16 = JMIntechEN16(midiChannel: 0, startCC: 32, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);
        this.pbf41 = JMIntechPBF4(midiChannel: 0, startCC: 64, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);
        this.pbf42 = JMIntechPBF4(midiChannel: 0, startCC: 88, oscServAddr: "127.0.0.1", oscServPort: 9000, postMIDIOSC: this.postMIDIOSC);
    }
}