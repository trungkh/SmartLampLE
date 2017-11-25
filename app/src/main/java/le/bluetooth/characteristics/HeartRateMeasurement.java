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

import java.util.ArrayList;

import le.bluetooth.smartgattlib.GattByteBuffer;
import le.bluetooth.smartgattlib.GattUtils;

public class HeartRateMeasurement {

    ArrayList<Float> rrIntervals = new ArrayList<Float>();
    int hrmval = 0;
    int eeval = -1;
    SensorWorn sensorWorn = SensorWorn.UNSUPPORTED;

    public enum SensorWorn {
        UNSUPPORTED, WORN, NOT_WORN
    }

    public HeartRateMeasurement(byte[] value) {
        GattByteBuffer bb = GattByteBuffer.wrap(value);
        byte flags = bb.getInt8();
        if (isHeartRateInUINT16(flags)) {
            hrmval = bb.getUint16();
        } else {
            hrmval = bb.getUint8();
        }
        if (isWornStatusPresent(flags)) {
            if (isSensorWorn(flags)) {
                sensorWorn = SensorWorn.WORN;
            } else {
                sensorWorn = SensorWorn.NOT_WORN;
            }
        }
        if (isEePresent(flags)) {
            eeval = bb.getUint16();
        }
        if (isRrIntPresent(flags)) {
            while (bb.hasRemaining()) {
                rrIntervals.add(bb.getUint16() / 1024F);
            }
        }
    }

   private boolean isHeartRateInUINT16(byte flags) {
        return (flags & GattUtils.FIRST_BITMASK) != 0;
    }

    private boolean isWornStatusPresent(byte flags) {
        return (flags & GattUtils.THIRD_BITMASK) != 0;
    }

    private boolean isSensorWorn(byte flags) {
        return (flags & GattUtils.SECOND_BITMASK) != 0;
    }

    private boolean isEePresent(byte flags) {
        return (flags & GattUtils.FOURTH_BITMASK) != 0;
    }

    private boolean isRrIntPresent(byte flags) {
        return (flags & GattUtils.FIFTH_BITMASK) != 0;
    }

    /**
     * @return RR-Intervals. Units: seconds
     */
    public ArrayList<Float> getRrInterval() {
        return rrIntervals;
    }

    public int getHr() {
        return hrmval;
    }

    /**
     * @return Energy Expended, Units: kilo Joules
     */
    public int getEe() {
        return eeval;
    }

    public SensorWorn getSensorWorn() {
        return sensorWorn;
    }
}
