/*
 * Copyright 2015 Trung Huynh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package le.bluetooth.characteristics;

import le.bluetooth.smartgattlib.GattByteBuffer;

public class BodySensorLocation {
    Location location = Location.Other;

    public enum Location {
        Other, Chest, Wrist, Finger, Hand, Ear_Lobe, Foot
    }

    public BodySensorLocation(byte[] value) {
        int loc = GattByteBuffer.wrap(value).getUint8();

        switch (loc) {
        case 0:
            location = Location.Other;
            break;
        case 1:
            location = Location.Chest;
            break;
        case 2:
            location = Location.Wrist;
            break;
        case 3:
            location = Location.Finger;
            break;
        case 4:
            location = Location.Hand;
            break;
        case 5:
            location = Location.Ear_Lobe;
            break;
        case 6:
            location = Location.Foot;
            break;
        }
    }

    /**
     * @return The current location of the sensor
     */
    public Location getBodySensorLocation() {
        return location;
    }
}
