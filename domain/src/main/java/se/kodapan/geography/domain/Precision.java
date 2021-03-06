/*
   Copyright 2010 Kodapan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package se.kodapan.geography.domain;

import java.io.*;

/**
 * @author kalle
 * @since 2010-jun-24 14:26:57
 */
public enum Precision {

  // from google

  /** indicates that the returned result is a precise geocode for which we have location information accurate down to street address precision. */
  ROOFTOP,

  /** indicates that the returned result reflects an approximation (usually on a road) interpolated between two precise points (such as intersections). Interpolated results are generally returned when rooftop geocodes are unavailable for a street address. */
  RANGE_INTERPOLATED,

  /**  indicates that the returned result is the geometric center of a result such as a polyline (for example, a street) or polygon (region). */
  GEOMETRIC_CENTER,

  /** indicates that the returned result is approximate. */
  APPROXIMATE,

  /** indicates that we have no clue what so ever to where in the bounds the location really is */
  WAY_OFF;

  public int integerValue() {
    switch (this) {
      case ROOFTOP: return 0;
      case RANGE_INTERPOLATED: return 1;
      case GEOMETRIC_CENTER: return 2;
      case APPROXIMATE: return 3;
      case WAY_OFF: return 4;
      default: return Integer.MAX_VALUE;
    }
  }

  public boolean morePreciseThan(Precision precision) {
    if (precision == null) {
      return true;
    }
    return precision.integerValue() > integerValue();
  }

  public boolean morePreciseThanOrEqualTo(Precision precision) {
    if (precision == null) {
      return true;
    }
    return precision.integerValue() >= integerValue();
  }


  private static final long serialVersionUID = 1l;
  
}
