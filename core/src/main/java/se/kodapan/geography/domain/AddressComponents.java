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

import se.kodapan.collections.MapSet;

import java.io.Serializable;
import java.util.*;

/**
 * @author kalle
 * @since 2010-jul-20 11:36:09
 */
public class AddressComponents extends ArrayList<AddressComponent> implements Serializable {

  private static final long serialVersionUID = 1l;

  private MapSet<AddressComponentType, AddressComponent> typeIndex = new MapSet<AddressComponentType, AddressComponent>();
  private String formattedAddress;

  public AddressComponents() {
    super(10);
  }

  public AddressComponents(Collection<? extends AddressComponent> addressComponents) {
    super(addressComponents);
    reconstructIndex();
  }

  public void reconstructIndex() {
    MapSet<AddressComponentType, AddressComponent> typeIndex = new MapSet<AddressComponentType, AddressComponent>();
    for (AddressComponent component : this) {
      for (AddressComponentType type : component.getTypes()) {
        typeIndex.add(type, component);
      }
    }
    this.typeIndex = typeIndex;
  }

  @Override
  public boolean removeAll(Collection<?> objects) {
    boolean success = super.removeAll(objects);
    if (success) {
      for (Object object : objects) {
        removeFromIndex((AddressComponent) object);
      }
    }
    return success;
  }

  @Override
  protected void removeRange(int i, int i1) {
    reconstructIndex();
  }


  /**
   * @param component
   * @return false if instance already exist in the list
   */
  @Override
  public boolean add(AddressComponent component) {
    if (contains(component)) {
      return false;
    }
    boolean success = super.add(component);
    if (component != null && success) {
      indexComponent(component);
    }
    return success;
  }

  /**
   * @param addressComponents
   * @return false is any of the instances already exist in the list
   */
  @Override
  public boolean addAll(Collection<? extends AddressComponent> addressComponents) {
    boolean success = true;
    for (AddressComponent component : addressComponents) {
      if (contains(component)) {
        success = false;
      } else if (add(component)) {
        indexComponent(component);
      } else {
        success = false;
      }
    }
    return success;
  }

  @Override
  public boolean addAll(int i, Collection<? extends AddressComponent> addressComponents) {
    throw new UnsupportedOperationException(); // todo
  }

  @Override
  public AddressComponent remove(int i) {
    AddressComponent component = super.remove(i);
    if (component != null) {
      removeFromIndex(component);
    }
    return component;
  }

  public boolean removeAll(AddressComponentType... types) {
    return removeAll(list(types));
  }

  @Override
  public boolean remove(Object o) {
    AddressComponent component = (AddressComponent) o;
    boolean success = super.remove(component);
    if (success) {
      removeFromIndex(component);
    }
    return success;
  }

  private void removeFromIndex(AddressComponent component) {
    for (AddressComponentType type : component.getTypes()) {
      typeIndex.get(type).remove(component);
    }
  }

  @Override
  public void clear() {
    super.clear();
    typeIndex.clear();
  }

  private void indexComponent(AddressComponent component) {
    for (AddressComponentType type : component.getTypes()) {
      typeIndex.add(type, component);
    }
  }

  @Override
  public void add(int i, AddressComponent component) {
    super.add(i, component);
    if (component != null) {
      indexComponent(component);
    }
  }

  @Override
  public AddressComponent set(int i, AddressComponent component) {
    AddressComponent previous = super.set(i, component);
    if (previous != null) {
      typeIndex.removeSetValue(previous);
    }
    if (component != null) {
      indexComponent(component);
    }
    return previous;
  }

  public AddressComponent getUnique(AddressComponentType... types) {
    List<AddressComponent> list = list(types);
    if (list.size() == 0) {
      return null;
    } else if (list.size() == 1) {
      return list.get(0);
    } else {
      throw new NoSuchElementException("Expected 0 or 1 but found " + list.size() + " address componentes with type " + Arrays.asList(types));
    }
  }


  public AddressComponent get(Collection<AddressComponentType> types) {
    List<AddressComponent> list = list(types);
    if (list.size() != 1) {
      return null;
    } else {
      return list.get(0);
    }
  }


  public AddressComponent get(AddressComponentType... types) {
    return get(Arrays.asList(types));
  }

  public List<AddressComponent> list(AddressComponentType... types) {
    return list(Arrays.asList(types));
  }

  public List<AddressComponent> list(Collection<AddressComponentType> types) {
    List<AddressComponent> list = new ArrayList<AddressComponent>();
    Set<AddressComponent> set = typeIndex.get(types.iterator().next());
    if (set != null && set.size() > 0) {
      for (AddressComponent component : set) {
        if (component.getTypes().containsAll(types)) {
          list.add(component);
        }
      }
    }
    return list;
  }

  public void setFormattedAddress(Locale locale) {
    StringBuilder sb = new StringBuilder();

    AddressComponent route = get(AddressComponentType.route);

    AddressComponent streetNumber = get(AddressComponentType.street_number);
    if (streetNumber != null) {
      if (locale.getLanguage().equals("en")) {

        sb.append(streetNumber.getLongName());
        sb.append(" ");
        sb.append(route.getLongName());
        sb.append(", ");


      } else {

        if (route == null) {
          System.currentTimeMillis();
        }
        sb.append(route.getLongName());
        sb.append(" ");
        sb.append(streetNumber.getLongName());
        sb.append(", ");

      }

    } else {

      if (route != null) {
        sb.append(route.getLongName());
        sb.append(", ");

      } else {
        AddressComponent streetAddress = get(AddressComponentType.street_address);
        if (streetAddress != null) {
          sb.append(streetAddress.getLongName());
          sb.append(", ");
        }

      }

    }


    for (AddressComponent subLocality : list(AddressComponentType.sublocality, AddressComponentType.political)) {
      if (subLocality != null) {
        sb.append(subLocality.getLongName());
        sb.append(", ");
      }
    }

    // todo find the most inner postal code, but only where there is one most inner postal code in the result. there might be several for a city, etc.
    List<AddressComponent> postalCodes = list(AddressComponentType.postal_code);
    if (postalCodes != null && postalCodes.size() == 1) {
      AddressComponent postalCode = postalCodes.iterator().next();
      sb.append(postalCode.getLongName());
      sb.append(", ");
    }

    AddressComponent postalTown = get(AddressComponentType.postal_town, AddressComponentType.political);
    if (postalTown != null) {
      sb.append(postalTown.getLongName());
      sb.append(", ");
    }

    AddressComponent city = get(AddressComponentType.locality, AddressComponentType.political);
    if (city != null) {
      sb.append(city.getLongName());
      sb.append(", ");
    }

    AddressComponent admin1 = get(AddressComponentType.administrative_area_level_1, AddressComponentType.political);
    if (admin1 != null) {
      sb.append(admin1.getLongName());
      sb.append(", ");
    }


    AddressComponent country = get(AddressComponentType.country);
    if (country != null) {
      sb.append(country.getLongName());
      sb.append(", ");
    }

    if (sb.length() == 0) {
      System.currentTimeMillis();
    }

    sb.delete(sb.length() - 2, sb.length());

    setFormattedAddress(sb.toString());

  }


  public MapSet<AddressComponentType, AddressComponent> getTypeIndex() {
    return typeIndex;
  }

  public void setTypeIndex(MapSet<AddressComponentType, AddressComponent> typeIndex) {
    this.typeIndex = typeIndex;
  }

  public String getFormattedAddress() {
    return formattedAddress;
  }

  public void setFormattedAddress(String formattedAddress) {
    this.formattedAddress = formattedAddress;
  }

  @Override
  public String toString() {
    return "AddressComponents{" +
        "formattedAddress='" + formattedAddress + '\'' +
        ", items=" + super.toString() +
        '}';
  }
}
