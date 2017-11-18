package le.bluetooth.characteristics;

import le.bluetooth.smartgattlib.GattByteBuffer;

public class ManufacturerNameString {
	String content = "";

	public ManufacturerNameString(byte[] value) {
		content = GattByteBuffer.wrap(value).getString();
	}

	public String getManufacturerNameString() {
		return content;
	}
}
