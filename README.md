# JMMIDIOSC

JMMIDIOSC is a SuperCollider extension that provides MIDI and OSC functionalities for controlling elements such as encoders and buttons.

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

The `JOOSCManager` class provides OSC functionalities for sending OSC messages.

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

}