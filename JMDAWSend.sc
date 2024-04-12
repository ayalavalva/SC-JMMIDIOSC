JMDAWSend : JMDAW {
    var <>name, <>number;
    var <>audioBus;
    var <>group, <>fxGroup;
    var <>mixer2x2;
    var <>faderControlBus;

    *new { |name, number, audioBus, group, fxGroup, mixer2x2, faderControlBus|
        ^super.new.init(name, number, audioBus, group, fxGroup, mixer2x2, faderControlBus)
    }

    init { |name, number, audioBus, group, fxGroup, mixer2x2, faderControlBus|
        this.name = name;
        this.number = number;
        this.audioBus = audioBus;
        this.group = group;
        this.fxGroup = fxGroup;
        this.mixer2x2 = mixer2x2;
        this.faderControlBus = faderControlBus;
    }

}