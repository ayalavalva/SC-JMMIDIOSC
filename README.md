# JMMIDIOSC

JMMIDIOSC is a SuperCollider extension that provides a set of classes to use Intech Studio MIDI controllers (PO16, EN16, PBF4) and send their MIDI date via OSC protocol, and control external MIDI devices (MIDI synths).

## Class tree

```plaintext
├── JMIntechControllers.sc # Abstract superclass for Intech Studio MIDI controllers
│ ├── JMIntechPO16.sc # PO16 controller class
│ ├── JMIntechEN16.sc # EN16 controller class
│ └── JMIntechPBF4.sc # PBF4 controller class
├── JMMIDIElements.sc # Abstract superclass for MIDI elements
│ ├── JMElementPotentiometer.sc # Potentiometer class
│ ├── JMElementEncoder.sc # Encoder class
│ ├── JMElementFader.sc # Fader class
│ └── JMElementButton.sc # Button class
└── JMOSCManager.sc # OSC manager class

├── JMMIDIDevices.sc # Abstract superclass for MIDI devices
└ └─── JMKorgVolcaDrum.sc # Korg Volca Drum class
```

## JMIntechControllers

### Overview

The `JMIntechControllers` class is an abstract superclass class which provides a set of methods and properties for controlling Intech Studio MIDI controllers (PO16, EN16, PBF4) and handling OSC communication. These controllers are composed of MIDI elements (potentiometers, encoders, faders and buttons).

### Properties

- `fullName`: The full name of the controller.
- `shortName`: The short name of the controller.
- `midiChannel`: The MIDI channel used for communication.
- `oscServAddr`: The OSC server address.
- `oscServPort`: The OSC server port.
- `potCount`: The number of potentiometers of the controller.
- `encCount`: The number of encoders of the controller.
- `fadCount`: The number of faders of the controller.
- `butCount`: The number of buttons of the controller.
- `controlBusDict`: A dictionary that maps for each MIDI element, their control bus keys to their corresponding control buses.
- `deviceNumb`: The device number (when connecting more than 1 device of the same type).

### Methods

- `*new(fullName, shortName, midiChannel, oscServAddr, oscServPort)`: Constructor method for creating a new `JMIntechControllers` instance.
- `init(fullName, shortName, midiChannel, oscServAddr, oscServPort)`: Initialization method for setting the properties of the instance.
- `buildElementsDict()`: Method for building the `controlBusDict` dictionary based on the number of MIDI elements (potentiometers, encoders, faders, buttons).
- `controlBus(key)`: Method for retrieving the control bus associated with a given key (a given element).
- `size()`: Method for getting the size of the `controlBusDict` dictionary.
- `free()`: Method for freeing the control buses and decrementing the device number.

### Usage

The `JMIntechControllers` class is not used directly. It is used as an abstract superclass for the `JMIntechPO16`, `JMIntechEN16` and `JMIntechPBF4` classes.

## JMElementButton

The `JMElementButton` class represents a MIDI button element. It inherits from the `JMMIDIElements` class.

### Properties

- `cc`: The MIDI control change number of the button.
- `ccValue`: The current value of the MIDI control change.
- `controlBus`: The control bus associated with the button.

### Methods

- `new(name, deviceNumb, elementNumber, midiChannel, cc)`: Creates a new `JMElementButton` instance.
- `initButton(cc)`: Initializes the button with the specified MIDI control change number.
- `midiReceiver`: Handles MIDI control change events for the button.
- `mappedMIDIValuetoControlBus`: Maps the MIDI value to the control bus and updates the control bus value.

## JMOSCManager

The `JMOSCManager` class provides OSC functionalities for sending OSC messages to an OSC server.

### Properties

- `oscServAddr`: The OSC server address.
- `oscServPort`: The OSC server port.
- `oscAddr`: The OSC address.

### Methods

- `new(oscServAddr, oscServPort)`: Creates a new `JMOSCManager` instance.
- `getSharedInstance(oscServAddr, oscServPort)`: Gets or creates the shared instance of `JMOSCManager`.
- `send(oscPath, args)`: Sends an OSC message with the specified OSC path and arguments.

## Usage

To use the JMMIDIOSC extension, follow these steps:

Use to send OSC messages in patch files:

1. Create an instance of `JMOSCManager` by calling `JMOSCManager(oscServAddr, oscServPort)`.
2. Send OSC messages using the `send` method of `JMOSCManager`.

Use with Intech Studio MIDI controllers:

1. Create an instance of `JMIntechPO16` (or any other Intech Studio MIDI controllers), by calling `JMIntechPO16.new(fullName, shortName, midiChannel, oscServAddr, oscServPort)` (i.e. `~po16 = JOIntechPO16(midiChannel: 0, startCC: 0, oscServAddr: "127.0.0.1", oscServPort: 9000);`).
2. Interpret `JMIntechPO16.sendBusOSC("/some/osc/path/"), \elementindex)` (i.e. `~pbf41.sendBusOSC("/pbf4_1/fa1", \FA1);`).

For more information, refer to the SuperCollider documentation.