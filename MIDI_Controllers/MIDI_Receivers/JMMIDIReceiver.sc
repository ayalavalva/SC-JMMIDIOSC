// an abstract receiver
JMMIDIReceiver {
    var <>element;
    
    *new { |element| 
        ^super.new.init(element) 
    }
    
    init { |element| 
        this.element = element;

        this.receiveMidiValue;
    }

    receiveMidiValue {
        "must override".error;
    }
    
    // called once at element init
    setup { 
        "must override".error; 
    }
    
    // called by the MIDIdef callback
    handle { |...args| 
        "must override".error; 
    }
}