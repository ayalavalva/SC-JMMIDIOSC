JMMIDI7bitReceiver {
    var <>channel;
    var <>ccNum;
    var <>rawValue;
    var <>minNorm;
    var <>maxNorm;
    var <>normalizedValue;
    var <>midiDefRef;

    *new { |channel, ccNum, minNorm = 0, maxNorm = 1|
        ^super.newCopyArgs(channel, ccNum, minNorm, maxNorm).installReceiver;
    }

    installReceiver { |channel, ccNum, minNorm = 0, maxNorm = 1|
        this.midiDefRef = MIDIdef.cc("Chan%CC%".format(this.channel, this.ccNum).asSymbol, { |val|
            this.rawValue = val;
            this.normalizedValue = val.linlin(0, 127, this.minNorm, this.maxNorm);
        }, ccNum: this.ccNum, chan: this.channel);
    }

    free {
        midiDefRef.free;
    }
}