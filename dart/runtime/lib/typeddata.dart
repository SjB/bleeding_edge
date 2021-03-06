// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

// patch classes for Int8List ..... Float64List and ByteData implementations.

patch class Int8List {
  /* patch */ factory Int8List(int length) {
    return new _Int8Array(length);
  }

  /* patch */ factory Int8List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Int8List.view(ByteBuffer buffer,
                                    [int offsetInBytes = 0, int length]) {
    return new _Int8ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalInt8Array _newTransferable(int length) {
    return new _ExternalInt8Array(length);
  }
}


patch class Uint8List {
  /* patch */ factory Uint8List(int length) {
    return new _Uint8Array(length);
  }

  /* patch */ factory Uint8List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Uint8List.view(ByteBuffer buffer,
                                     [int offsetInBytes = 0, int length]) {
    return new _Uint8ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalUint8Array _newTransferable(int length) {
    return new _ExternalUint8Array(length);
  }
}


patch class Uint8ClampedList {
  /* patch */ factory Uint8ClampedList(int length) {
    return new _Uint8ClampedArray(length);
  }

  /* patch */ factory Uint8ClampedList.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Uint8ClampedList.view(ByteBuffer buffer,
                                            [int offsetInBytes = 0,
                                             int length]) {
    return new _Uint8ClampedArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalUint8ClampedArray _newTransferable(int length) {
    return new _ExternalUint8ClampedArray(length);
  }
}


patch class Int16List {
  /* patch */ factory Int16List(int length) {
    return new _Int16Array(length);
  }

  /* patch */ factory Int16List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Int16List.view(ByteBuffer buffer,
                                     [int offsetInBytes = 0, int length]) {
    return new _Int16ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalInt16Array _newTransferable(int length) {
    return new _ExternalInt16Array(length);
  }
}


patch class Uint16List {
  /* patch */ factory Uint16List(int length) {
    return new _Uint16Array(length);
  }

  /* patch */ factory Uint16List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Uint16List.view(ByteBuffer buffer,
                                      [int offsetInBytes = 0, int length]) {
    return new _Uint16ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalUint16Array _newTransferable(int length) {
    return new _ExternalUint16Array(length);
  }
}


patch class Int32List {
  /* patch */ factory Int32List(int length) {
    return new _Int32Array(length);
  }

  /* patch */ factory Int32List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Int32List.view(ByteBuffer buffer,
                                     [int offsetInBytes = 0, int length]) {
    return new _Int32ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalInt32Array _newTransferable(int length) {
    return new _ExternalInt32Array(length);
  }
}


patch class Uint32List {
  /* patch */ factory Uint32List(int length) {
    return new _Uint32Array(length);
  }

  /* patch */ factory Uint32List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Uint32List.view(ByteBuffer buffer,
                                      [int offsetInBytes = 0, int length]) {
    return new _Uint32ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalUint32Array _newTransferable(int length) {
    return new _ExternalUint32Array(length);
  }
}


patch class Int64List {
  /* patch */ factory Int64List(int length) {
    return new _Int64Array(length);
  }

  /* patch */ factory Int64List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Int64List.view(ByteBuffer buffer,
                                     [int offsetInBytes = 0, int length]) {
    return new _Int64ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalInt64Array _newTransferable(int length) {
    return new _ExternalInt64Array(length);
  }
}


patch class Uint64List {
  /* patch */ factory Uint64List(int length) {
    return new _Uint64Array(length);
  }

  /* patch */ factory Uint64List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Uint64List.view(ByteBuffer buffer,
                                      [int offsetInBytes = 0, int length]) {
    return new _Uint64ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalUint64Array _newTransferable(int length) {
    return new _ExternalUint64Array(length);
  }
}


patch class Float32List {
  /* patch */ factory Float32List(int length) {
    return new _Float32Array(length);
  }

  /* patch */ factory Float32List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Float32List.view(ByteBuffer buffer,
                                       [int offsetInBytes = 0, int length]) {
    return new _Float32ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalFloat32Array _newTransferable(int length) {
    return new _ExternalFloat32Array(length);
  }
}


patch class Float64List {
  /* patch */ factory Float64List(int length) {
    return new _Float64Array(length);
  }

  /* patch */ factory Float64List.transferable(int length) {
    return _newTransferable(length);
  }

  /* patch */ factory Float64List.view(ByteBuffer buffer,
                                       [int offsetInBytes = 0, int length]) {
    return new _Float64ArrayView(buffer, offsetInBytes, length);
  }

  static _ExternalFloat64Array _newTransferable(int length) {
    return new _ExternalFloat64Array(length);
  }
}


patch class ByteData {
  /* patch */ factory ByteData(int length) {
    var list = new _Uint8Array(length);
    return new _ByteDataView(list.buffer);
  }

  /* patch */ factory ByteData.transferable(int length) {
    var list = new _Uint8Array.transferable(length);
    return new _ByteDataView(list.buffer);
  }

  /* patch */ factory ByteData.view(ByteBuffer buffer,
                                    [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = buffer.lengthInBytes;
    }
    return new _ByteDataView(buffer, offsetInBytes, length);
  }
}


// Based class for _TypedList that provides common methods for implementing
// the collection and list interfaces.

abstract class _TypedListBase {
  // Method(s) implementing the Collection interface.
  bool contains(element) => IterableMixinWorkaround.contains(this, element);

  void forEach(void f(element)) {
    var len = this.length;
    for (var i = 0; i < len; i++) {
      f(this[i]);
    }
  }

  Iterable map(f(int element)) {
    return IterableMixinWorkaround.mapList(this, f);
  }

  String join([String separator]) {
    return IterableMixinWorkaround.join(this, separator);
  }

  dynamic reduce(dynamic initialValue,
                 dynamic combine(dynamic initialValue, element)) {
    return IterableMixinWorkaround.reduce(this, initialValue, combine);
  }

  Collection where(bool f(int element)) {
    return IterableMixinWorkaround.where(this, f);
  }

  Iterable expand(Iterable f(int element)) {
    return IterableMixinWorkaround.expand(this, f);
  }

  Iterable take(int n) {
    return IterableMixinWorkaround.takeList(this, n);
  }

  Iterable takeWhile(bool test(int value)) {
    return IterableMixinWorkaround.takeWhile(this, test);
  }

  Iterable skip(int n) {
    return IterableMixinWorkaround.skipList(this, n);
  }

  Iterable skipWhile(bool test(int value)) {
    return IterableMixinWorkaround.skipWhile(this, test);
  }

  bool every(bool f(element)) {
    return IterableMixinWorkaround.every(this, f);
  }

  bool any(bool f(element)) {
    return IterableMixinWorkaround.any(this, f);
  }

  int firstWhere(bool test(int value), {int orElse()}) {
    return IterableMixinWorkaround.firstWhere(this, test, orElse);
  }

  int lastWhere(bool test(int value), {int orElse()}) {
    return IterableMixinWorkaround.lastWhereList(this, test, orElse);
  }

  int singleWhere(bool test(int value)) {
    return IterableMixinWorkaround.singleWhere(this, test);
  }

  int elementAt(int index) {
    return this[index];
  }

  bool get isEmpty {
    return this.length == 0;
  }


  // Method(s) implementing the List interface.

  set length(newLength) {
    throw new UnsupportedError(
        "Cannot resize a non-extendable array");
  }

  void add(value) {
    throw new UnsupportedError(
        "Cannot add to a non-extendable array");
  }

  void addLast(value) {
    throw new UnsupportedError(
        "Cannot add to a non-extendable array");
  }

  void addAll(Iterable value) {
    throw new UnsupportedError(
        "Cannot add to a non-extendable array");
  }

  void sort([int compare(var a, var b)]) {
    return IterableMixinWorkaround.sortList(this, compare);
  }

  int indexOf(element, [int start = 0]) {
    return IterableMixinWorkaround.indexOfList(this, element, start);
  }

  int lastIndexOf(element, [int start = null]) {
    return IterableMixinWorkaround.lastIndexOfList(this, element, start);
  }

  void clear() {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  int removeLast() {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  void remove(Object element) {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  void removeAll(Iterable elements) {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  void retainAll(Iterable elements) {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  void removeWhere(bool test(int element)) {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  void retainWhere(bool test(int element)) {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  int get first {
    if (length > 0) return this[0];
    throw new StateError("No elements");
  }

  int get last {
    if (length > 0) return this[length - 1];
    throw new StateError("No elements");
  }

  int get single {
    if (length == 1) return this[0];
    if (length == 0) throw new StateError("No elements");
    throw new StateError("More than one element");
  }

  int min([int compare(int a, int b)]) =>
    IterableMixinWorkaround.min(this, compare);

  int max([int compare(int a, int b)]) =>
    IterableMixinWorkaround.max(this, compare);

  void removeRange(int start, int length) {
    throw new UnsupportedError(
        "Cannot remove from a non-extendable array");
  }

  void insertRange(int start, int length, [initialValue]) {
    throw new UnsupportedError(
        "Cannot add to a non-extendable array");
  }

  List toList() {
    return new List.from(this);
  }

  Set toSet() {
    return new Set.from(this);
  }

  List sublist(int start, [int end]) {
    if (end == null) end = length;
    int length = end - start;
    _rangeCheck(this.length, start, length);
    List result = _createList(length);
    result.setRange(0, length, this, start);
    return result;
  }

  List getRange(int start, int length) {
    return sublist(start, start + length);
  }

  void setRange(int start, int length, List from, [int startFrom = 0]) {
    if (!_setRange(start, length, from, startFrom)) {
      IterableMixinWorkaround.setRangeList(this, start,
                                           length, from, startFrom);
    }
  }


  // Method(s) implementing Object interface.

  String toString() {
    return Collections.collectionToString(this);
  }


  // Internal utility methods.

  bool _setRange(int start, int length, List from, startFrom)
      native "TypedData_setRange";
}


abstract class _TypedList extends _TypedListBase implements ByteBuffer {
  // Default method implementing parts of the TypedData interface.
  int get offsetInBytes {
    return 0;
  }

  int get lengthInBytes {
    return length * elementSizeInBytes;
  }

  ByteBuffer get buffer {
    return this;
  }


  // Methods implementing the collection interface.

  int get length native "TypedData_length";


  // Internal utility methods.

  int _getInt8(int offsetInBytes) native "TypedData_GetInt8";
  void _setInt8(int offsetInBytes, int value) native "TypedData_SetInt8";

  int _getUint8(int offsetInBytes) native "TypedData_GetUint8";
  void _setUint8(int offsetInBytes, int value) native "TypedData_SetUint8";

  int _getInt16(int offsetInBytes) native "TypedData_GetInt16";
  void _setInt16(int offsetInBytes, int value) native "TypedData_SetInt16";

  int _getUint16(int offsetInBytes) native "TypedData_GetUint16";
  void _setUint16(int offsetInBytes, int value) native "TypedData_SetUint16";

  int _getInt32(int offsetInBytes) native "TypedData_GetInt32";
  void _setInt32(int offsetInBytes, int value) native "TypedData_SetInt32";

  int _getUint32(int offsetInBytes) native "TypedData_GetUint32";
  void _setUint32(int offsetInBytes, int value) native "TypedData_SetUint32";

  int _getInt64(int offsetInBytes) native "TypedData_GetInt64";
  void _setInt64(int offsetInBytes, int value) native "TypedData_SetInt64";

  int _getUint64(int offsetInBytes) native "TypedData_GetUint64";
  void _setUint64(int offsetInBytes, int value) native "TypedData_SetUint64";

  double _getFloat32(int offsetInBytes) native "TypedData_GetFloat32";
  void _setFloat32(int offsetInBytes, double value)
      native "TypedData_SetFloat32";

  double _getFloat64(int offsetInBytes) native "TypedData_GetFloat64";
  void _setFloat64(int offsetInBytes, double value)
      native "TypedData_SetFloat64";
}


class _Int8Array extends _TypedList implements Int8List {
  // Factory constructors.

  factory _Int8Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Int8Array.view(ByteBuffer buffer,
                          [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = buffer.lengthInBytes;
    }
    return new _Int8ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getInt8(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setInt8(index, _toInt8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Int8List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Int8Array _createList(int length) {
    return _new(length);
  }

  static _Int8Array _new(int length) native "TypedData_Int8Array_new";
}


class _Uint8Array extends _TypedList implements Uint8List {
  // Factory constructors.

  factory _Uint8Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Uint8Array.view(ByteBuffer buffer,
                           [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = buffer.lengthInBytes;
    }
    return new _Uint8ArrayView(buffer, offsetInBytes, length);
  }


  // Methods implementing List interface.
  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getUint8(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setUint8(index, _toUint8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Methods implementing TypedData interface.
  int get elementSizeInBytes {
    return Uint8List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Uint8Array _createList(int length) {
    return _new(length);
  }

  static _Uint8Array _new(int length) native "TypedData_Uint8Array_new";
}


class _Uint8ClampedArray extends _TypedList implements Uint8ClampedList {
  // Factory constructors.

  factory _Uint8ClampedArray(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Uint8ClampedArray.view(ByteBuffer buffer,
                                  [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = buffer.lengthInBytes;
    }
    return new _Uint8ClampedArrayView(buffer, offsetInBytes, length);
  }


  // Methods implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getUint8(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setUint8(index, _toClampedUint8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Methods implementing TypedData interface.
  int get elementSizeInBytes {
    return Uint8List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Uint8ClampedArray _createList(int length) {
    return _new(length);
  }

  static _Uint8ClampedArray _new(int length)
      native "TypedData_Uint8ClampedArray_new";
}


class _Int16Array extends _TypedList implements Int16List {
  // Factory constructors.

  factory _Int16Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Int16Array.view(ByteBuffer buffer,
                           [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
               Int16List.BYTES_PER_ELEMENT;
    }
    return new _Int16ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedInt16(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedInt16(index, _toInt16(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Int16List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Int16Array _createList(int length) {
    return _new(length);
  }

  int _getIndexedInt16(int index) {
    return _getInt16(index * Int16List.BYTES_PER_ELEMENT);
  }

  void _setIndexedInt16(int index, int value) {
    _setInt16(index * Int16List.BYTES_PER_ELEMENT, value);
  }

  static _Int16Array _new(int length) native "TypedData_Int16Array_new";
}


class _Uint16Array extends _TypedList implements Uint16List {
  // Factory constructors.

  factory _Uint16Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Uint16Array.view(ByteBuffer buffer,
                            [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
                Uint16List.BYTES_PER_ELEMENT;
    }
    return new _Uint16ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedUint16(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedUint16(index, _toUint16(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint16List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Uint16Array _createList(int length) {
    return _new(length);
  }

  int _getIndexedUint16(int index) {
    return _getUint16(index * Uint16List.BYTES_PER_ELEMENT);
  }

  void _setIndexedUint16(int index, int value) {
    _setUint16(index * Uint16List.BYTES_PER_ELEMENT, value);
  }

  static _Uint16Array _new(int length) native "TypedData_Uint16Array_new";
}


class _Int32Array extends _TypedList implements Int32List {
  // Factory constructors.

  factory _Int32Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Int32Array.view(ByteBuffer buffer,
                           [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
                Int32List.BYTES_PER_ELEMENT;
    }
    return new _Int32ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedInt32(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedInt32(index, _toInt32(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Int32List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Int32Array _createList(int length) {
    return _new(length);
  }

  int _getIndexedInt32(int index) {
    return _getInt32(index * Int32List.BYTES_PER_ELEMENT);
  }

  void _setIndexedInt32(int index, int value) {
    _setInt32(index * Int32List.BYTES_PER_ELEMENT, value);
  }

  static _Int32Array _new(int length) native "TypedData_Int32Array_new";
}


class _Uint32Array extends _TypedList implements Uint32List {
  // Factory constructors.

  factory _Uint32Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Uint32Array.view(ByteBuffer buffer,
                            [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
                Uint32List.BYTES_PER_ELEMENT;
    }
    return new _Uint32ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedUint32(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedUint32(index, _toUint32(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint32List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Uint32Array _createList(int length) {
    return _new(length);
  }

  int _getIndexedUint32(int index) {
    return _getUint32(index * Uint32List.BYTES_PER_ELEMENT);
  }

  void _setIndexedUint32(int index, int value) {
    _setInt32(index * Uint32List.BYTES_PER_ELEMENT, value);
  }

  static _Uint32Array _new(int length) native "TypedData_Uint32Array_new";
}


class _Int64Array extends _TypedList implements Int64List {
  // Factory constructors.

  factory _Int64Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Int64Array.view(ByteBuffer buffer,
                           [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
                Int32List.BYTES_PER_ELEMENT;
    }
    return new _Int64ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedInt64(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedInt64(index, _toInt64(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Int64List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Int64Array _createList(int length) {
    return _new(length);
  }

  int _getIndexedInt64(int index) {
    return _getInt64(index * Int64List.BYTES_PER_ELEMENT);
  }

  void _setIndexedInt64(int index, int value) {
    _setInt64(index * Int64List.BYTES_PER_ELEMENT, value);
  }

  static _Int64Array _new(int length) native "TypedData_Int64Array_new";
}


class _Uint64Array extends _TypedList implements Uint64List {
  // Factory constructors.

  factory _Uint64Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Uint64Array.view(ByteBuffer buffer,
                            [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
               Uint64List.BYTES_PER_ELEMENT;
    }
    return new _Uint64ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedUint64(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedUint64(index, _toUint64(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint64List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Uint64Array _createList(int length) {
    return _new(length);
  }

  int _getIndexedUint64(int index) {
    return _getUint64(index * Uint64List.BYTES_PER_ELEMENT);
  }

  void _setIndexedUint64(int index, int value) {
    _setUint64(index * Uint64List.BYTES_PER_ELEMENT, value);
  }

  static _Uint64Array _new(int length) native "TypedData_Uint64Array_new";
}


class _Float32Array extends _TypedList implements Float32List {
  // Factory constructors.

  factory _Float32Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Float32Array.view(ByteBuffer buffer,
                             [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
               Float32List.BYTES_PER_ELEMENT;
    }
    return new _Float32ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing the List interface.

  double operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedFloat32(index);
  }

  void operator[]=(int index, double value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedFloat32(index, value);
  }

  Iterator<double> get iterator {
    return new _TypedListIterator<double>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Float32List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Float32Array _createList(int length) {
    return _new(length);
  }

  double _getIndexedFloat32(int index) {
    return _getFloat32(index * Float32List.BYTES_PER_ELEMENT);
  }

  void _setIndexedFloat32(int index, double value) {
    _setFloat32(index * Float32List.BYTES_PER_ELEMENT, value);
  }

  static _Float32Array _new(int length) native "TypedData_Float32Array_new";
}


class _Float64Array extends _TypedList implements Float64List {
  // Factory constructors.

  factory _Float64Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }

  factory _Float64Array.view(ByteBuffer buffer,
                             [int offsetInBytes = 0, int length]) {
    if (length == null) {
      length = (buffer.lengthInBytes - offsetInBytes) ~/
               Float64List.BYTES_PER_ELEMENT;
    }
    return new _Float64ArrayView(buffer, offsetInBytes, length);
  }


  // Method(s) implementing the List interface.

  double operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedFloat64(index);
  }

  void operator[]=(int index, double value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedFloat64(index, value);
  }

  Iterator<double> get iterator {
    return new _TypedListIterator<double>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Float64List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  _Float64Array _createList(int length) {
    return _new(length);
  }

  double _getIndexedFloat64(int index) {
    return _getFloat64(index * Float64List.BYTES_PER_ELEMENT);
  }

  void _setIndexedFloat64(int index, double value) {
    _setFloat64(index * Float64List.BYTES_PER_ELEMENT, value);
  }

  static _Float64Array _new(int length) native "TypedData_Float64Array_new";
}


class _ExternalInt8Array extends _TypedList implements Int8List {
  // Factory constructors.

  factory _ExternalInt8Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.
  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getInt8(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setInt8(index, value);
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Int8List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Int8List _createList(int length) {
    return new Int8List(length);
  }

  static _ExternalInt8Array _new(int length) native
      "ExternalTypedData_Int8Array_new";
}


class _ExternalUint8Array extends _TypedList implements Uint8List {
  // Factory constructors.

  factory _ExternalUint8Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getUint8(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setUint8(index, _toUint8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint8List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Uint8List _createList(int length) {
    return new Uint8List(length);
  }

  static _ExternalUint8Array _new(int length) native
      "ExternalTypedData_Uint8Array_new";
}


class _ExternalUint8ClampedArray extends _TypedList implements Uint8ClampedList {
  // Factory constructors.

  factory _ExternalUint8ClampedArray(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getUint8(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setUint8(index, _toClampedUint8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint8List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Uint8ClampedList _createList(int length) {
    return new Uint8ClampedList(length);
  }

  static _ExternalUint8ClampedArray _new(int length) native
      "ExternalTypedData_Uint8ClampedArray_new";
}


class _ExternalInt16Array extends _TypedList implements Int16List {
  // Factory constructors.

  factory _ExternalInt16Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedInt16(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedInt16(index, _toInt16(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Int16List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Int16List _createList(int length) {
    return new Int16List(length);
  }

  int _getIndexedInt16(int index) {
    return _getInt16(index * Int16List.BYTES_PER_ELEMENT);
  }

  void _setIndexedInt16(int index, int value) {
    _setInt16(index * Int16List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalInt16Array _new(int length) native
      "ExternalTypedData_Int16Array_new";
}


class _ExternalUint16Array extends _TypedList implements Uint16List {
  // Factory constructors.

  factory _ExternalUint16Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedUint16(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedUint16(index, _toUint16(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint16List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Uint16List _createList(int length) {
    return new Uint16List(length);
  }

  int _getIndexedUint16(int index) {
    return _getUint16(index * Uint16List.BYTES_PER_ELEMENT);
  }

  void _setIndexedUint16(int index, int value) {
    _setUint16(index * Uint16List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalUint16Array _new(int length) native
      "ExternalTypedData_Uint16Array_new";
}


class _ExternalInt32Array extends _TypedList implements Int32List {
  // Factory constructors.

  factory _ExternalInt32Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedInt32(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedInt32(index, _toInt32(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Int32List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Int32List _createList(int length) {
    return new Int32List(length);
  }

  int _getIndexedInt32(int index) {
    return _getInt32(index * Int32List.BYTES_PER_ELEMENT);
  }

  void _setIndexedInt32(int index, int value) {
    _setInt32(index * Int32List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalInt32Array _new(int length) native
      "ExternalTypedData_Int32Array_new";
}


class _ExternalUint32Array extends _TypedList implements Uint32List {
  // Factory constructors.

  factory _ExternalUint32Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedUint32(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedUint32(index, _toUint32(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint32List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Uint32List _createList(int length) {
    return new Uint32List(length);
  }

  int _getIndexedUint32(int index) {
    return _getUint32(index * Uint32List.BYTES_PER_ELEMENT);
  }

  void _setIndexedUint32(int index, int value) {
    _setInt32(index * Uint32List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalUint32Array _new(int length) native
      "ExternalTypedData_Uint32Array_new";
}


class _ExternalInt64Array extends _TypedList implements Int64List {
  // Factory constructors.

  factory _ExternalInt64Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedInt64(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedInt64(index, _toInt64(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Int64List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Int64List _createList(int length) {
    return new Int64List(length);
  }

  int _getIndexedInt64(int index) {
    return _getInt64(index * Int64List.BYTES_PER_ELEMENT);
  }

  void _setIndexedInt64(int index, int value) {
    _setInt64(index * Int64List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalInt64Array _new(int length) native
      "ExternalTypedData_Int64Array_new";
}


class _ExternalUint64Array extends _TypedList implements Uint64List {
  // Factory constructors.

  factory _ExternalUint64Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedUint64(index);
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedUint64(index, _toUint64(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Uint64List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Uint64List _createList(int length) {
    return new Uint64List(length);
  }

  int _getIndexedUint64(int index) {
    return _getUint64(index * Uint64List.BYTES_PER_ELEMENT);
  }

  void _setIndexedUint64(int index, int value) {
    _setUint64(index * Uint64List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalUint64Array _new(int length) native
      "ExternalTypedData_Uint64Array_new";
}


class _ExternalFloat32Array extends _TypedList implements Float32List {
  // Factory constructors.

  factory _ExternalFloat32Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  double operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedFloat32(index);
  }

  void operator[]=(int index, double value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedFloat32(index, value);
  }

  Iterator<double> get iterator {
    return new _TypedListIterator<double>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Float32List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Float32List _createList(int length) {
    return new Float32List(length);
  }

  double _getIndexedFloat32(int index) {
    return _getFloat32(index * Float32List.BYTES_PER_ELEMENT);
  }

  void _setIndexedFloat32(int index, double value) {
    _setFloat32(index * Float32List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalFloat32Array _new(int length) native
      "ExternalTypedData_Float32Array_new";
}


class _ExternalFloat64Array extends _TypedList implements Float64List {
  // Factory constructors.

  factory _ExternalFloat64Array(int length) {
    if (length < 0) {
      String message = "$length must be greater than 0";
      throw new ArgumentError(message);
    }
    return _new(length);
  }


  // Method(s) implementing the List interface.

  double operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _getIndexedFloat64(index);
  }

  void operator[]=(int index, double value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _setIndexedFloat64(index, value);
  }

  Iterator<double> get iterator {
    return new _TypedListIterator<double>(this);
  }


  // Method(s) implementing the TypedData interface.

  int get elementSizeInBytes {
    return Float64List.BYTES_PER_ELEMENT;
  }


  // Internal utility methods.

  Float64List _createList(int length) {
    return new Float64List(length);
  }

  double _getIndexedFloat64(int index) {
    return _getFloat64(index * Float64List.BYTES_PER_ELEMENT);
  }

  void _setIndexedFloat64(int index, double value) {
    _setFloat64(index * Float64List.BYTES_PER_ELEMENT, value);
  }

  static _ExternalFloat64Array _new(int length) native
      "ExternalTypedData_Float64Array_new";
}


class _TypedListIterator<E> implements Iterator<E> {
  final List<E> _array;
  final int _length;
  int _position;
  E _current;

  _TypedListIterator(List array)
      : _array = array, _length = array.length, _position = -1 {
    assert(array is _TypedList || array is _TypedListView);
  }

  bool moveNext() {
    int nextPosition = _position + 1;
    if (nextPosition < _length) {
      _current = _array[nextPosition];
      _position = nextPosition;
      return true;
    }
    _position = _length;
    _current = null;
    return false;
  }

  E get current => _current;
}


class _TypedListView extends _TypedListBase implements TypedData {
  _TypedListView(ByteBuffer _buffer, int _offset, int _length)
    : _typeddata = _buffer,  // This assignment is type safe.
      offsetInBytes = _offset,
      length = _length {
  }


  // Method(s) implementing the TypedData interface.

  int get lengthInBytes {
    return length * elementSizeInBytes;
  }

  ByteBuffer get buffer {
    return _typeddata.buffer;
  }

  final TypedData _typeddata;
  final int offsetInBytes;
  final int length;
}


class _Int8ArrayView extends _TypedListView implements Int8List {
  // Constructor.
  _Int8ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Int8List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                _offsetInBytes,
                _length * Int8List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getInt8(offsetInBytes +
                               (index * Int8List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata. setInt8(offsetInBytes + (index * Int8List.BYTES_PER_ELEMENT),
                        _toInt8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Int8List.BYTES_PER_ELEMENT;
  }
}


class _Uint8ArrayView extends _TypedListView implements Uint8List {
  // Constructor.
  _Uint8ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Uint8List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                _offsetInBytes,
                _length * Uint8List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getUint8(offsetInBytes +
                                (index * Uint8List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setUint8(offsetInBytes + (index * Uint8List.BYTES_PER_ELEMENT),
                         _toUint8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Uint8List.BYTES_PER_ELEMENT;
  }
}


class _Uint8ClampedArrayView extends _TypedListView implements Uint8List {
  // Constructor.
  _Uint8ClampedArrayView(ByteBuffer buffer,
                         [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Uint8List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Uint8List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getUint8(offsetInBytes +
                                (index * Uint8List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setUint8(offsetInBytes + (index * Uint8List.BYTES_PER_ELEMENT),
                         _toClampedUint8(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Uint8List.BYTES_PER_ELEMENT;
  }
}


class _Int16ArrayView extends _TypedListView implements Int16List {
  // Constructor.
  _Int16ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Int16List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Int16List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getInt16(offsetInBytes +
                                (index * Int16List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setInt16(offsetInBytes + (index * Int16List.BYTES_PER_ELEMENT),
                         _toInt16(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Int16List.BYTES_PER_ELEMENT;
  }
}


class _Uint16ArrayView extends _TypedListView implements Uint16List {
  // Constructor.
  _Uint16ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Uint16List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Uint16List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getUint16(offsetInBytes +
                                 (index * Uint16List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setUint16(offsetInBytes + (index * Uint16List.BYTES_PER_ELEMENT),
                          _toUint16(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Uint16List.BYTES_PER_ELEMENT;
  }
}


class _Int32ArrayView extends _TypedListView implements Int32List {
  // Constructor.
  _Int32ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Int32List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Int32List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getInt32(offsetInBytes +
                                (index * Int32List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setInt32(offsetInBytes + (index * Int32List.BYTES_PER_ELEMENT),
                         _toInt32(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Int32List.BYTES_PER_ELEMENT;
  }
}


class _Uint32ArrayView extends _TypedListView implements Uint32List {
  // Constructor.
  _Uint32ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Uint32List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Uint32List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getUint32(offsetInBytes +
                                 (index * Uint32List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setUint32(offsetInBytes + (index * Uint32List.BYTES_PER_ELEMENT),
                          _toUint32(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Uint32List.BYTES_PER_ELEMENT;
  }
}


class _Int64ArrayView extends _TypedListView implements Int64List {
  // Constructor.
  _Int64ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Int64List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Int64List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getInt64(offsetInBytes +
                                (index * Int64List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setInt64(offsetInBytes + (index * Int64List.BYTES_PER_ELEMENT),
                         _toInt64(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Int64List.BYTES_PER_ELEMENT;
  }
}


class _Uint64ArrayView extends _TypedListView implements Uint64List {
  // Constructor.
  _Uint64ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Uint64List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Uint64List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  int operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getUint64(offsetInBytes +
                                 (index * Uint64List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, int value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setUint64(offsetInBytes + (index * Uint64List.BYTES_PER_ELEMENT),
                          _toUint64(value));
  }

  Iterator<int> get iterator {
    return new _TypedListIterator<int>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Uint64List.BYTES_PER_ELEMENT;
  }
}


class _Float32ArrayView extends _TypedListView implements Float32List {
  // Constructor.
  _Float32ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Float32List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Float32List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  double operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getFloat32(offsetInBytes +
                                  (index * Float32List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, double value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setFloat32(offsetInBytes +
                           (index * Float32List.BYTES_PER_ELEMENT), value);
  }

  Iterator<double> get iterator {
    return new _TypedListIterator<double>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Float32List.BYTES_PER_ELEMENT;
  }
}


class _Float64ArrayView extends _TypedListView implements Float64List {
  // Constructor.
  _Float64ArrayView(ByteBuffer buffer, [int _offsetInBytes = 0, int _length])
    : super(buffer, _offsetInBytes,
            _defaultIfNull(_length,
                           ((buffer.lengthInBytes - _offsetInBytes) ~/
                            Float64List.BYTES_PER_ELEMENT))) {
    _rangeCheck(buffer.lengthInBytes,
                offsetInBytes,
                length * Float64List.BYTES_PER_ELEMENT);
  }


  // Method(s) implementing List interface.

  double operator[](int index) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    return _typeddata._getFloat64(offsetInBytes +
                                  (index * Float64List.BYTES_PER_ELEMENT));
  }

  void operator[]=(int index, double value) {
    if (index < 0 || index >= length) {
      String message = "$index must be in the range [0..$length)";
      throw new RangeError(message);
    }
    _typeddata._setFloat64(offsetInBytes +
                          (index * Float64List.BYTES_PER_ELEMENT), value);
  }

  Iterator<double> get iterator {
    return new _TypedListIterator<double>(this);
  }


  // Method(s) implementing TypedData interface.

  int get elementSizeInBytes {
    return Float64List.BYTES_PER_ELEMENT;
  }
}


class _ByteDataView implements ByteData {
  _ByteDataView(ByteBuffer _buffer, int _offsetInBytes, int _lengthInBytes)
    : _typeddata = _buffer as TypedData,
      _offset = _offsetInBytes,
      _length = _lengthInBytes {
    _rangeCheck(_buffer.lengthInBytes, _offset, _length);
  }


  // Method(s) implementing TypedData interface.

  ByteBuffer get buffer {
    return _typeddata.buffer;
  }

  int get lengthInBytes {
    return _length;
  }

  int offsetInBytes() {
    return _offset;
  }

  // Method(s) implementing ByteData interface.

  int getInt8(int byteOffset) {
    return _typeddata._getInt8(_offset + byteOffset);
  }
  void setInt8(int byteOffset, int value) {
    _typeddata._setInt8(_offset + byteOffset, value);
  }

  int getUint8(int byteOffset) {
    return _typeddata._getUint8(_offset + byteOffset);
  }
  void setUint8(int byteOffset, int value) {
    _typeddata._setUint8(_offset + byteOffset, value);
  }

  int getInt16(int byteOffset) {
    return _typeddata._getInt16(_offset + byteOffset);
  }
  void setInt16(int byteOffset, int value) {
    _typeddata._setInt16(_offset + byteOffset, value);
  }

  int getUint16(int byteOffset) {
    return _typeddata._getUint16(_offset + byteOffset);
  }
  void setUint16(int byteOffset, int value) {
    _typeddata._setUint16(_offset + byteOffset, value);
  }

  int getInt32(int byteOffset) {
    return _typeddata._getInt32(_offset + byteOffset);
  }
  void setInt32(int byteOffset, int value) {
    _typeddata._setInt32(_offset + byteOffset, value);
  }

  int getUint32(int byteOffset) {
    return _typeddata._getUint32(_offset + byteOffset);
  }
  void setUint32(int byteOffset, int value) {
    _typeddata._setUint32(_offset + byteOffset, value);
  }

  int getInt64(int byteOffset) {
    return _typeddata._getInt64(_offset + byteOffset);
  }
  void setInt64(int byteOffset, int value) {
    _typeddata._setInt64(_offset + byteOffset, value);
  }

  int getUint64(int byteOffset) {
    return _typeddata._getUint64(_offset + byteOffset);
  }
  void setUint64(int byteOffset, int value) {
    _typeddata._setUint64(_offset + byteOffset, value);
  }

  double getFloat32(int byteOffset) {
    return _typeddata._getFloat32(_offset + byteOffset);
  }
  void setFloat32(int byteOffset, double value) {
    _typeddata._setFloat32(_offset + byteOffset, value);
  }

  double getFloat64(int byteOffset) {
    return _typeddata._getFloat64(_offset + byteOffset);
  }
  void setFloat64(int byteOffset, double value) {
    _typeddata._setFloat64(_offset + byteOffset, value);
  }

  final TypedData _typeddata;
  final int _offset;
  final int _length;
}


// Top level utility methods.
int _toInt(int value, int mask) {
  value &= mask;
  if (value > (mask >> 1)) value -= mask + 1;
  return value;
}


int _toInt8(int value) {
  return _toInt(value, 0xFF);
}


int _toUint8(int value) {
  return value & 0xFF;
}


int _toClampedUint8(int value) {
  if (value < 0) return 0;
  if (value > 0xFF) return 0xFF;
  return value;
}


int _toInt16(int value) {
  return _toInt(value, 0xFFFF);
}


int _toUint16(int value) {
  return value & 0xFFFF;
}


int _toInt32(int value) {
  return _toInt(value, 0xFFFFFFFF);
}


int _toUint32(int value) {
  return value & 0xFFFFFFFF;
}


int _toInt64(int value) {
  return _toInt(value, 0xFFFFFFFFFFFFFFFF);
}


int _toUint64(int value) {
  return value & 0xFFFFFFFFFFFFFFFF;
}


void _rangeCheck(int listLength, int start, int length) {
  if (length < 0) {
    throw new RangeError.value(length);
  }
  if (start < 0) {
    throw new RangeError.value(start);
  }
  if (start + length > listLength) {
    throw new RangeError.value(start + length);
  }
}


int _defaultIfNull(object, value) {
  if (object == null) {
    return value;
  }
  return object;
}
