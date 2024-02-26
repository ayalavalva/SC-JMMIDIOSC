# JMMIDIOSC

JMMIDIOSC is a SuperCollider extension that provides a set of classes for Intech Studio MIDI controllers (PO16, EN16, PBF4) to control MIDI elements such as potentiometers, encoders, faders and buttons with MIDI and OSC functionalities.

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
```

## JMIntechControllers

### Overview
The `JMIntechControllers` class is an abstract superclass class which provides a set of methods and properties for handling MIDI and OSC communication.

### Properties
- `fullName`: The full name of the controller.
- `shortName`: The short name of the controller.
- `midiChannel`: The MIDI channel used for communication.
- `oscServAddr`: The OSC server address.
- `oscServPort`: The OSC server port.
- `potCount`: The number of potentiometers.
- `encCount`: The number of encoders.
- `fadCount`: The number of faders.
- `butCount`: The number of buttons.
- `controlBusDict`: A dictionary that maps control bus keys to their corresponding control buses.
- `deviceNumb`: The device number.

### Methods
- `*new(fullName, shortName, midiChannel, oscServAddr, oscServPort)`: Constructor method for creating a new `JMIntechControllers` instance.
- `init(fullName, shortName, midiChannel, oscServAddr, oscServPort)`: Initialization method for setting the properties of the instance.
- `buildElementsDict()`: Method for building the `controlBusDict` dictionary based on the number of elements (potentiometers, encoders, faders, buttons).
- `controlBus(key)`: Method for retrieving the control bus associated with a given key.
- `size()`: Method for getting the size of the `controlBusDict` dictionary.
- `free()`: Method for freeing the control buses and decrementing the device number.

### Usage
To use the `JMIntechControllers` class, create an instance by calling the constructor method `*new` and pass the required parameters. Then, call the `buildElementsDict` method to build the control bus dictionary. You can access the control buses using the `controlBus` method and perform operations on them. Finally, when you're done using the instance, call the `free` method to free the control buses and decrement the device number.

## JOElementButton

The `JOElementButton` class represents a MIDI button element. It inherits from the `JOMIDIElements` class.

### Properties

- `cc`: The MIDI control change number of the button.
- `ccValue`: The current value of the MIDI control change.
- `controlBus`: The control bus associated with the button.

### Methods

- `new(name, deviceNumb, elementNumber, midiChannel, cc)`: Creates a new `JOElementButton` instance.
- `initButton(cc)`: Initializes the button with the specified MIDI control change number.
- `midiReceiver`: Handles MIDI control change events for the button.
- `mappedMIDIValuetoControlBus`: Maps the MIDI value to the control bus and updates the control bus value.

## JOOSCManager

The `JOOSCManager` class provides OSC functionalities for sending OSC messages to an OSC server.

### Properties

- `oscServAddr`: The OSC server address.
- `oscServPort`: The OSC server port.
- `oscAddr`: The OSC address.

### Methods

- `new(oscServAddr, oscServPort)`: Creates a new `JOOSCManager` instance.
- `getSharedInstance(oscServAddr, oscServPort)`: Gets or creates the shared instance of `JOOSCManager`.
- `send(oscPath, args)`: Sends an OSC message with the specified OSC path and arguments.

## Usage

To use the JMMIDIOSC extension, follow these steps:

1. Create an instance of `JOOSCManager` by calling `JOOSCManager.getSharedInstance(oscServAddr, oscServPort)`.
2. Create an instance of `JOElementButton` by calling `JOElementButton.new(name, deviceNumb, elementNumber, midiChannel, cc)`.
3. Initialize the button by calling `initButton(cc)`.
4. Handle MIDI control change events by implementing the `midiReceiver` method.
5. Map the MIDI value to the control bus and update the control bus value by calling `mappedMIDIValuetoControlBus`.
6. Send OSC messages using the `send` method of `JOOSCManager`.

For more information, refer to the SuperCollider documentation.