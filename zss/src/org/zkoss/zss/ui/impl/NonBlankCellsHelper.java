/* HeaderPositionHelper.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 9, 2008 12:35:40 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A utility class for calculating position of non-blank cells.
 * Each non-blank cells of a row or a column is stored as a {@link NonBlankCellInfo} in this helper.  
 * @author henrichen
 * @since 3.8.1
 */
//ZSS-1000
public class NonBlankCellsHelper {

	int _defaultSize;
	private List<NonBlankCellInfo> _infos;

	public NonBlankCellsHelper(List<NonBlankCellInfo> infos) {
		this._infos = infos;
	}
	
	public List<NonBlankCellInfo> getInfos() {
		return new ArrayList<NonBlankCellInfo>(_infos);
	}

	public boolean isNonBlank(int cellIndex) {
		final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new NonBlankCellInfoComparator());
		return j >= 0 ? true : _isNonBlank(-j-1-1, cellIndex);
	}
	
	public int getNextNonBlank(int start) {
		int j = getListIndex(start);
		
		if (j < 0) {
			j = -j - 1;
			if (j >= _infos.size()) { //out of bound
				return -1;
			}
			NonBlankCellInfo info = _infos.get(j);
			return info.start;
		} else {
			NonBlankCellInfo info = _infos.get(j);
			if (start >= info.end) {
				j = j + 1;
				if (j >= _infos.size()) { //out of bound
					return -1;
				}
				info = _infos.get(j);
				return info.start;
			} else {
				return info.end; //ZSS-1109
			}
		}
	}
	
	public int getPrevNonBlank(int start) {
		int j = getListIndex(start);
		
		if (j < 0) {
			j = -j - 1 - 1;
			if (j < 0) { //out of bound
				return -1;
			}
			NonBlankCellInfo info = _infos.get(j);
			return info.end;
		} else {
			NonBlankCellInfo info = _infos.get(j);
			if (start <= info.start) {
				j = j - 1;
				if (j < 0) { //out of bound
					return -1;
				}
				info = _infos.get(j);
				return info.end;
			} else {
				return info.start; //ZSS-1109
			}
		}
	}
	
	// check whether the cellIndex is within the range of the 
	//   NonBlankCellInfo specified by the listIndex
	private boolean _isNonBlank(int listIndex, int cellIndex) {
		if (listIndex < 0 || listIndex >= _infos.size()) return false; //out of _infos range
		NonBlankCellInfo info = _infos.get(listIndex);
		return info.isNonBlank(cellIndex);
	}

	//given target cell index, return list index. 
	private int getListIndex(int cellIndex) {
		final int j = Collections.binarySearch(_infos, Integer.valueOf(cellIndex), new NonBlankCellInfoComparator());
		return j >= 0 || !_isNonBlank(-j - 1 -1, cellIndex) ? j : -j - 1 - 1;
	}

	//Return the associated NonBlankCellInfo of the specified cell index.
	public NonBlankCellInfo getInfo(int cellIndex) {
		int j = getListIndex(cellIndex);
		return j < 0 ? null : _infos.get(j); 
	}

//	
//	public void shiftMeta(int cellIndex, int offset) {
//		int index = getListIndex(cellIndex);
//		if (index < 0) { //no intercept
//			index = -index - 1;
//		} else {
//			final NonBlankCellInfo info = _infos.get(index);
//			if (info.start < cellIndex) {
//				//split the info to two
//				final int end0 = info.end;
//				info.end = cellIndex - 1;
//				index += 1;
//				_infos.add(index, new NonBlankCellInfo(cellIndex, end0));
//			}
//		}
//		for (int j = _infos.size() - 1; j >= index; --j) {
//			final NonBlankCellInfo info = _infos.get(j);
//			info.shift(offset);
//		}
//	}
//
//	public void unshiftMeta(int cellIndex, int offset) {
//		final int endCellIndex = cellIndex + offset - 1;
//		int bindex = getListIndex(cellIndex);
//		int eindex = getListIndex(endCellIndex);
//		
//		if (bindex >= 0 && bindex == eindex) {
//			//       |-------|
//			//case1     |--|
//			//case2     |----|
//			//case3  |-----|
//			//case4  |-------|
//			final NonBlankCellInfo info = _infos.get(bindex);
//			if (info.start < cellIndex) {
//				//case1 or case2
//				if (info.end > endCellIndex) {
//					//case1
//					info.end = info.end - offset;
//				} else {
//					info.end = cellIndex - 1;
//				}
//				bindex += 1;
//			} else {
//				//case3 or case4
//				if (info.end > endCellIndex) {
//					//case3
//					info.start = endCellIndex + 1;
//					bindex += 1;
//				} else { //case4
//					_infos.remove(eindex);
//				}
//				eindex -= 1;
//			}
//		} else {
//			//            |---| .... |---|
//			// case1            |--|
//			// case2            |------|
//			// case3         |-----|
//			// case4         |---------|
//			if (bindex < 0) { // no intercept
//				bindex = -bindex - 1;
//			} else {
//				final NonBlankCellInfo binfo = _infos.get(bindex);
//				if (binfo.start < cellIndex) {
//					//split the info
//					binfo.end = cellIndex - 1;
//					bindex += 1;
//				}
//			}
//			
//			if (eindex < 0) { // no intercept
//				eindex = -eindex - 1 - 1;
//			} else {
//				final NonBlankCellInfo einfo = _infos.get(eindex);
//				if (einfo.end > endCellIndex) {
//					//split the info
//					einfo.start = endCellIndex + 1;
//					eindex -= 1;
//				}
//			}
//			
//			//remove in between
//			for (int j = eindex; j >= bindex; --j) {
//				_infos.remove(j);
//			}
//		}
//		
//		//shift the end
//		for (int j = _infos.size() - 1; j >= bindex; --j) {
//			final NonBlankCellInfo info = _infos.get(j);
//			info.shift(-offset);
//		}
//		
//		//merge
//		if (bindex < _infos.size()) {
//			final NonBlankCellInfo curr = _infos.get(bindex);
//			if (bindex + 1 < _infos.size()) {
//				final NonBlankCellInfo next = _infos.get(bindex+1);
//				if (curr.end + 1 == next.start) { //should merge
//					curr.end = next.end;
//					_infos.remove(bindex+1);
//				}
//			}
//			if (bindex > 0 ) {
//				final NonBlankCellInfo prev = _infos.get(bindex-1);
//				if (prev.end + 1 == curr.start) { //should merge
//					prev.end = curr.end;
//					_infos.remove(bindex);
//				}
//			}
//		}
//	}
//	
//	//set new info values at the specified range; if not exist, create a new one and add into this Helper.
//	public void setInfoValues(int start, int end) {
//		int bindex = getListIndex(start);
//		int eindex = getListIndex(end);
//		
//		// already set; return
//		// |------|
//		//   |--|
//		if (bindex > 0 && bindex == eindex) {
//			return;
//		}
//
//		//            |---| .... |---|
//		// case1            |--|
//		// case2            |------|
//		// case3         |-----|
//		// case4         |---------|
//		
//		//remove infos between bindex and eindex
//		final int bindex0 = bindex < 0 ? -bindex-1 : bindex+1;
//		final int eindex0 = eindex < 0 ? -eindex-1-1 : eindex-1;
//		for (int j = eindex0; j >= bindex0; --j) {
//			_infos.remove(j);
//		}
//		
//		if (bindex < 0) { // no intercept on start
//			//case1 or case2
//			if (eindex < 0) { // no intercept on end
//				//case1
//				final NonBlankCellInfo info = new NonBlankCellInfo(start, end);
//				_infos.add(bindex0, info);
//			} else {
//				//case2
//				final NonBlankCellInfo info = _infos.get(bindex0);
//				info.start = start;
//			}
//			bindex = bindex0;
//		} else {
//			final NonBlankCellInfo info = _infos.get(bindex);
//			//case 3 or case 4
//			if (eindex < 0) { // no intercept on end
//				//case3
//				info.end = end;
//			} else {
//				//case4
//				final NonBlankCellInfo einfo = _infos.get(bindex+1);
//				info.end = einfo.end;
//				_infos.remove(bindex+1);
//			}
//		}
//
//		//merge
//		final NonBlankCellInfo curr = _infos.get(bindex);
//		if (bindex + 1 < _infos.size()) {
//			final NonBlankCellInfo next = _infos.get(bindex + 1);
//			if (curr.end + 1 == next.start) {
//				curr.end = next.end;
//				_infos.remove(bindex + 1);
//			}
//		}
//		if (bindex > 0) {
//			final NonBlankCellInfo prev = _infos.get(bindex - 1);
//			if (prev.end + 1 == curr.start) {
//				prev.end = curr.end;
//				_infos.remove(bindex);
//			}
//		}
//	}
//
//	public void removeInfo(int start, int end) {
//		int bindex = getListIndex(start);
//		int eindex = getListIndex(end);
//		
//		// already set; return
//		//        |------|
//		// case1    |--|
//		// case2    |----|
//		// case3  |----|
//		// case4  |------|
//		if (bindex > 0 && bindex == eindex) {
//			final NonBlankCellInfo info = _infos.get(bindex);
//			if (info.start < start) {
//				//case1 or case2
//				if (end < info.end) {
//					//case1
//					final NonBlankCellInfo info0 = new NonBlankCellInfo(end+1, info.end);
//					_infos.add(bindex+1, info0);
//				}
//				info.end = start - 1;
//			} else {
//				//case3 or case4
//				if (end < info.end) {
//					//case3
//					info.start = end+1;
//				} else {
//					//case4
//					_infos.remove(bindex);
//				}
//			}
//			return;
//		}
//
//		//            |---| .... |---|
//		// case1            |--|
//		// case2            |------|
//		// case2.1          |--------|
//		// case3         |-----|
//		// case4         |---------|
//		
//		//remove infos between bindex and eindex
//		final int bindex0 = bindex < 0 ? -bindex-1 : bindex+1;
//		final int eindex0 = eindex < 0 ? -eindex-1-1 : eindex-1;
//		for (int j = eindex0; j >= bindex0; --j) {
//			_infos.remove(j);
//		}
//		
//		if (bindex < 0) {
//			//case1 or case2 or case2.1
//			if (eindex >= 0) {
//				//case2 or case2.1
//				final NonBlankCellInfo info = _infos.get(bindex0);
//				if (end < info.end) {
//					//case2
//					info.start = end + 1;
//				} else {
//					_infos.remove(bindex0);
//				}
//			}
//		} else {
//			final NonBlankCellInfo info = _infos.get(bindex);
//			//case3 or case4
//			if (eindex >= 0) {
//				//case4
//				final NonBlankCellInfo einfo = _infos.get(bindex+1);
//				if (end < einfo.end) {
//					einfo.start = end + 1;
//				} else {
//					_infos.remove(bindex+1);
//				}
//			}
//			if (info.start < start) {
//				info.end = start - 1;
//			} else {
//				_infos.remove(bindex);
//			}
//		}
//	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (NonBlankCellInfo info : _infos) {
			sb.append("[");
			sb.append(info.start).append(", ");
			sb.append(info.end);
			sb.append("],");

		}
		sb.append("]");
		return sb.toString();
	}
	
	public static class NonBlankCellInfo {
		//[0]: column/row index, [1]: width/height, [2]: column/row id
		public int start; //start column/row index that has non-blank cells
		public int end; //end column/row index that has non-blank cells
		
		public NonBlankCellInfo(int start, int end) {
			this.start = start;
			this.end = end;
		}
		
		public void shift(int offset) {
			start += offset;
			end += offset;
		}
		
		/**
		 * Returns whether the specified cellIndex is within range of this NonBlankCellInfo.
		 * @param cellIndex
		 * @return
		 */
		public boolean isNonBlank(int cellIndex) {
			return start <= cellIndex && cellIndex <= end;
		}
	}
	
	private static class NonBlankCellInfoComparator implements Comparator, Serializable {
		private static final long serialVersionUID = -1290415509269113184L;

		@Override
		public int compare(Object o1, Object o2) {
			final int i1 = o1 instanceof NonBlankCellInfo ? ((NonBlankCellInfo)o1).start: ((Integer)o1).intValue();
			final int i2 = o2 instanceof NonBlankCellInfo ? ((NonBlankCellInfo)o2).start: ((Integer)o2).intValue();
			return i1 - i2;
		} 
	}
}
