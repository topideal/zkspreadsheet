/* CellFetchCommandHelper.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		January 10, 2008 03:10:40 PM , Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under Lesser GPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;


import java.util.Map;

import org.zkoss.json.JSONObject;
import org.zkoss.lang.Objects;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.SheetCtrl;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.impl.HeaderPositionHelper;
import org.zkoss.zss.ui.impl.JSONObj;
import org.zkoss.zss.ui.impl.MergeMatrixHelper;
import org.zkoss.zss.ui.impl.Utils;
import org.zkoss.zss.ui.sys.SpreadsheetCtrl;
import org.zkoss.zss.ui.sys.SpreadsheetInCtrl;


/**
 * A Command Helper for (client to server) for fetch data back
 * @author Dennis.Chen
 *
 */
public class CellFetchCommandHelper{
	private static final Log log = Log.lookup(CellFetchCommandHelper.class);
	
	private Spreadsheet _spreadsheet;
	private SpreadsheetCtrl _ctrl;
	HeaderPositionHelper _rowHelper;
	HeaderPositionHelper _colHelper;
	private MergeMatrixHelper _mergeMatrix;
	private boolean _hidecolhead;
	private boolean _hiderowhead;
	private int _lastleft;
	private int _lastright;
	private int _lasttop;
	private int _lastbottom;
	
	private void responseDataBlock(String postfix, String token, String sheetid, String result) {
		//bug 1953830 Unnecessary command was sent and break the processing
		//use smartUpdate to instead
		//_spreadsheet.response(null, new org.zkoss.zss.ui.au.out.AuDataBlock(_spreadsheet,token,sheetid,result));
		
		//to avoid response be override in smartUpdate, I use a count-postfix
		//_spreadsheet.smartUpdateValues("dblock_"+Utils.nextUpdateId(),new Object[]{token,sheetid,result});
		
		_spreadsheet.smartUpdate(postfix != null ? "dataBlockUpdate" + postfix : "dataBlockUpdate", new String[] {token, sheetid, result});
	}
	
	//-- super --//
	protected void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		final Map data = request.getData();
		if (data == null || data.size() != 22)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
				new Object[] {Objects.toString(data), this});
		
		_spreadsheet = ((Spreadsheet)comp);
		if(_spreadsheet.isInvalidated()) return;//since it is invalidate, i don't need to update
		final Worksheet selSheet = _spreadsheet.getSelectedSheet();
		final String sheetId = (String) data.get("sheetId");
		if (selSheet == null || !sheetId.equals(((SheetCtrl)selSheet).getUuid())) { //not current selected sheet, skip.
			return;
		}
		
		_ctrl = ((SpreadsheetCtrl)_spreadsheet.getExtraCtrl());
		_hidecolhead = _spreadsheet.isHidecolumnhead();
		_hiderowhead = _spreadsheet.isHiderowhead();
		String token = (String) data.get("token");
		
		_rowHelper = _ctrl.getRowPositionHelper(sheetId);
		_colHelper = _ctrl.getColumnPositionHelper(sheetId);
		
		Worksheet sheet = _spreadsheet.getSelectedSheet();
		if(!Utils.getSheetUuid(sheet).equals(sheetId)) return;
		
		_mergeMatrix = _ctrl.getMergeMatrixHelper(sheet);
		
		String type = (String) data.get("type"); 
		String direction = (String) data.get("direction");
		
		
		int dpWidth = (Integer)data.get("dpWidth");//pixel value of data panel width
		int dpHeight = (Integer)data.get("dpHeight");//pixel value of data panel height
		int viewWidth = (Integer)data.get("viewWidth");//pixel value of view width(scrollpanel.clientWidth)
		int viewHeight = (Integer)data.get("viewHeight");//pixel value of value height
		
		int blockLeft = (Integer)data.get("blockLeft");
		int blockTop = (Integer)data.get("blockTop"); 
		int blockRight = (Integer)data.get("blockRight");// + blockLeft - 1;
		int blockBottom = (Integer)data.get("blockBottom");// + blockTop - 1;;
		
		int fetchLeft = (Integer)data.get("fetchLeft");
		int fetchTop = (Integer)data.get("fetchTop"); 
		int fetchWidth = (Integer)data.get("fetchWidth");
		int fetchHeight = (Integer)data.get("fetchHeight");
		
		int rangeLeft = (Integer)data.get("rangeLeft");//visible range
		int rangeTop = (Integer)data.get("rangeTop"); 
		int rangeRight = (Integer)data.get("rangeRight");
		int rangeBottom = (Integer)data.get("rangeBottom");
		
		int cacheRangeFetchTopHeight = (Integer)data.get("arFetchTopHeight");
		int cacheRangeFetchBtmHeight = (Integer)data.get("arFetchBtmHeight");
		
		try{
			if("jump".equals(type)){
				String result = null;
				if("east".equals(direction)){
					result = jump("E",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else if("south".equals(direction)){
					result = jump("S",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else if("west".equals(direction)){
					result = jump("W",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else if("north".equals(direction)){
					result = jump("N",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else if("westnorth".equals(direction)){
					result = jump("WN",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else if("eastnorth".equals(direction)){
					result = jump("EN",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else if("westsouth".equals(direction)){
					result = jump("WS",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else if("eastsouth".equals(direction)){
					result = jump("ES",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,rangeLeft,rangeTop,rangeRight,rangeBottom);
				}else{
					throw new UiException("Unknow direction:"+direction);
				}
				responseDataBlock("Jump", token, sheetId, result);
			} else if ("neighbor".equals(type)) {
				if("east".equals(direction)){
					
					int right = blockRight + fetchWidth ;//blockRight+ 1 + fetchWidth - 1;
					right = _mergeMatrix.getRightConnectedColumn(right,	blockTop, blockBottom);
					
					//check top for new loaded east block
					int bottom = _mergeMatrix.getBottomConnectedRow(blockBottom, blockLeft, right);
					int top = _mergeMatrix.getTopConnectedRow(blockTop, blockLeft, right);
					
					if (bottom > blockBottom) {
						String result = loadSouth(sheet, type, blockLeft, blockTop, blockRight, blockBottom, bottom - blockBottom, -1, -1, -1);
						responseDataBlock("South", "", sheetId, result);
					}
					if (top < blockTop) {
						String result = loadNorth(sheet, type, blockLeft, blockTop, blockRight, bottom, blockTop - top, -1, -1, -1);
						responseDataBlock("North", "", sheetId, result);
					}
					int size = right - blockRight;//right - (blockRight +1) +1

					LoadResult result = loadEast(sheet, type, blockLeft, top, blockRight, bottom, size, -1, cacheRangeFetchTopHeight, cacheRangeFetchBtmHeight);
					responseDataBlock("East", token, sheetId, result.json.toJSONString());
				} else if ("south".equals(direction)) {
					
					int bottom = blockBottom + fetchHeight;
					bottom = _mergeMatrix.getBottomConnectedRow(bottom, blockLeft, blockRight);
					
					//check right for new load south block
					int right = _mergeMatrix.getRightConnectedColumn(blockRight, blockTop, bottom);
					int left = _mergeMatrix.getLeftConnectedColumn(blockLeft, blockTop, bottom);

					if (right > blockRight) {
						LoadResult result = loadEast(sheet, type, blockLeft, blockTop, blockRight, blockBottom, right - blockRight, -1, -1, -1);
						responseDataBlock("East", "", sheetId, result.json.toJSONString());
					}
					if (left < blockLeft) {
						LoadResult result = loadWest(sheet, type, blockLeft, blockTop, right, blockBottom, blockLeft - left, -1, -1, -1);
						responseDataBlock("West", "", sheetId, result.json.toJSONString());
					}

					int size = bottom - blockBottom;
					
					String result = loadSouth(sheet, type, left, blockTop, right, blockBottom, size, -1, -1, -1);
					responseDataBlock("South", token, sheetId, result);
				} else if ("west".equals(direction)) {
					
					int left = blockLeft - fetchWidth ;//blockLeft - 1 - fetchWidth + 1;
					left = _mergeMatrix.getLeftConnectedColumn(left,blockTop,blockBottom);
					//check top-bottom for new load west block
					int bottom = _mergeMatrix.getBottomConnectedRow(blockBottom, left, blockRight);
					int top = _mergeMatrix.getTopConnectedRow(blockTop, left, blockRight);
					
					if (bottom > blockBottom) {
						String result = loadSouth(sheet, type, blockLeft, blockTop, blockRight, blockBottom, bottom - blockBottom, -1, -1, -1);
						responseDataBlock("South", "", sheetId, result);
					}
					if (top < blockTop) {
						String result = loadNorth(sheet, type, blockLeft, blockTop, blockRight, bottom, blockTop - top, -1, -1, -1);
						responseDataBlock("North", "", sheetId, result);
					}
					int size = blockLeft - left ;//blockLeft -1 - left + 1;
					
					LoadResult result = loadWest(sheet, type, blockLeft, blockTop,	blockRight, blockBottom, size, -1, cacheRangeFetchTopHeight, cacheRangeFetchBtmHeight);
					responseDataBlock("West", token, sheetId, result.json.toJSONString());
				} else if("north".equals(direction)) {
					
					
					int top = blockTop - fetchHeight;
					top = _mergeMatrix.getTopConnectedRow(top, blockLeft, blockRight);
					//check right-left for new load north block
					int right = _mergeMatrix.getRightConnectedColumn(blockRight, top, blockBottom);
					int left = _mergeMatrix.getLeftConnectedColumn(blockLeft,top, blockBottom);
					
					if (right > blockRight) {
						LoadResult result = loadEast(sheet, type, blockLeft, blockTop, blockRight, blockBottom, right - blockRight, -1, -1, -1);
						responseDataBlock("East", "", sheetId, result.json.toJSONString());
					}
					if (left < blockLeft) {
						LoadResult result = loadWest(sheet,type,blockLeft,blockTop,right,blockBottom,blockLeft - left, -1, -1, -1);
						responseDataBlock("West", "", sheetId, result.json.toJSONString());
					}
					int size = blockTop - top;
					String result = loadNorth(sheet, type, left, blockTop, right, blockBottom, size, -1, -1, -1);
					responseDataBlock("North", token, sheetId, result);
				}
			} else if("visible".equals(type)) {
				loadForVisible((Spreadsheet) comp, sheetId, sheet, type, dpWidth, dpHeight, viewWidth, viewHeight, blockLeft, blockTop, blockRight, blockBottom, rangeLeft, rangeTop, rangeRight, rangeBottom, fetchWidth, fetchHeight);
				//always ack for call back
				String ack = ackResult();
				responseDataBlock(null, token,sheetId,ack);
			} else {
				//TODO use debug warning
				log.warning("unknow type:"+type);
			}
				
		} catch(Throwable x) {
			responseDataBlock("Error", "", sheetId, ackError(x.getMessage()));
			throw new UiException(x.getMessage(), x);
		}
		((SpreadsheetInCtrl) _ctrl).setLoadedRect(_lastleft, _lasttop,	_lastright, _lastbottom);
	}
	
	private void loadForVisible(Spreadsheet spreadsheet, String sheetId, Worksheet sheet, String type, int dpWidth,
			int dpHeight, int viewWidth, int viewHeight, int blockLeft, int blockTop, int blockRight, int blockBottom,
			int rangeLeft, int rangeTop, int rangeRight, int rangeBottom, int cacheRangeWidth, int cacheRangeHeight) {
		
		if (rangeRight > spreadsheet.getMaxcolumns() - 1) {
			rangeRight = spreadsheet.getMaxcolumns() - 1;
		}
		if (rangeBottom > spreadsheet.getMaxrows() - 1) {
			rangeBottom = spreadsheet.getMaxrows() - 1;
		}
		//calculate visible range , for merge range.
		int left = Math.min(rangeLeft, blockLeft);
		int top = Math.min(rangeTop, blockTop);
		int right = Math.max(rangeRight, blockRight);
		int bottom = Math.max(rangeBottom, blockBottom);
		
		int cacheRangeRight = -1;
		int cacheRangeLeft = -1;

		boolean loadEast = right > blockRight;
		boolean loadWest = left < blockLeft;
		boolean loadSouth = bottom > blockBottom;
		boolean loadNorth = top < blockTop;
		if (loadEast) {
			
			right = _mergeMatrix.getRightConnectedColumn(right, top, bottom);

			int width = right - blockRight;
			LoadResult result = loadEast(sheet, type, blockLeft, blockTop, blockRight, blockBottom, width, cacheRangeWidth, -1, loadSouth && cacheRangeHeight > 0 ? -1 : cacheRangeHeight);
			cacheRangeRight = result.index;
			responseDataBlock("East", "", sheetId, result.json.toJSONString());
			blockRight += width;
		}
		if (loadWest) {

			left = _mergeMatrix.getLeftConnectedColumn(left, top, bottom);
			int size = blockLeft - left;
			LoadResult result = loadWest(sheet, type, blockLeft, blockTop, right, blockBottom, size, cacheRangeWidth, -1, loadSouth && cacheRangeHeight > 0 ? -1 : cacheRangeHeight);
			cacheRangeLeft = result.index;
			responseDataBlock("West", "", sheetId, result.json.toJSONString());
			blockLeft -= size;
		}
		if (loadSouth) {
			
			bottom = _mergeMatrix.getBottomConnectedRow(bottom, left, right);
			
			int height = bottom - blockBottom;
			String result = loadSouth(sheet, type, left, blockTop, right, blockBottom, height, cacheRangeLeft, cacheRangeRight, cacheRangeHeight);
			responseDataBlock("South", "", sheetId, result);
			blockBottom += height;
		}
		if (loadNorth) {
			
			top = _mergeMatrix.getTopConnectedRow(top, left, right);
			
			int size = blockTop - top;
			String result = loadNorth(sheet, type, left, blockTop, right, bottom, size, cacheRangeLeft, cacheRangeRight, cacheRangeHeight);
			responseDataBlock("North", "", sheetId, result);
			blockTop -= size;
		}
	}
	
	private String ackResult(){
		JSONObj jresult = new JSONObj();
		jresult.setData("type", "ack");
		return jresult.toString();
	}
	
	private String ackError(String message){
		JSONObj jresult = new JSONObj();
		jresult.setData("type","error");
		jresult.setData("message",message);
		return jresult.toString();
	}
	
	private String jumpResult(Worksheet sheet, int left, int top, int right, int bottom) {
		top = _mergeMatrix.getTopConnectedRow(top, left, right);
		bottom = _mergeMatrix.getBottomConnectedRow(bottom, left, right);
		right = _mergeMatrix.getRightConnectedColumn(right,top,bottom);
		left = _mergeMatrix.getLeftConnectedColumn(left,top,bottom);

		int w = right - left + 1;
		int h = bottom - top + 1;
		
		//check merge range;
		JSONObject json = new JSONObject();
		
		json.put("type", "jump");
		json.put("left", left);
		json.put("top", top);
		json.put("width", w);
		json.put("height", h);
		
		SpreadsheetCtrl.Header header = SpreadsheetCtrl.Header.NONE;
		if (!_hidecolhead && !_hiderowhead) {
			header = SpreadsheetCtrl.Header.BOTH;
		} else if (!_hidecolhead) {
			header = SpreadsheetCtrl.Header.COLUMN;
		} else if (!_hiderowhead) {
			header = SpreadsheetCtrl.Header.ROW;
		}
		
		int preloadColSize = _spreadsheet.getPreloadColumnSize();
		int preloadRowSize = _spreadsheet.getPreloadRowSize();
		
		int re = top + h;
		int ce = left + w;
		
		int rangeLeft = left;
		int rangeRight = right;
		int rangeTop = top;
		int rangeBtm = bottom;
		
		if (preloadColSize > 0 && preloadRowSize > 0) {
			//extends both
			preloadColSize = preloadColSize / 2;
			preloadRowSize = preloadRowSize / 2;
			
			int newLeft = Math.max(rangeLeft - preloadColSize, 0);
			int newTop = Math.max(rangeTop - preloadRowSize, 0);
			int newRight = Math.min(rangeRight + preloadColSize, _spreadsheet.getMaxcolumns() - 1);
			int newBtm = Math.min(rangeBtm + preloadRowSize, _spreadsheet.getMaxrows() - 1);
			
			rangeTop = _mergeMatrix.getTopConnectedRow(newTop, newLeft, newRight);
			rangeBtm = _mergeMatrix.getBottomConnectedRow(newBtm, newLeft, newRight);
			rangeRight = _mergeMatrix.getRightConnectedColumn(newRight, newTop, newBtm);
			rangeLeft = _mergeMatrix.getLeftConnectedColumn(newLeft, newTop, newBtm);
			
		} else if (preloadColSize > 0) {
			//extends range left and right
			int preloadSize =  preloadColSize / 2;
			int newLeft = Math.max(rangeLeft - preloadSize, 0);
			int newRight = Math.min(rangeRight + preloadSize, _spreadsheet.getMaxcolumns() - 1);
			
			rangeLeft = _mergeMatrix.getLeftConnectedColumn(newLeft, rangeTop, rangeBtm);
			rangeRight = _mergeMatrix.getRightConnectedColumn(newRight, rangeTop, rangeBtm);
		} else if (preloadRowSize > 0) {
			
			int preloadSize = preloadRowSize / 2;
			int newTop = Math.max(rangeTop - preloadSize, 0);
			int newBtm = Math.min(rangeBtm + preloadSize, _spreadsheet.getMaxrows() - 1);
			
			rangeTop = _mergeMatrix.getTopConnectedRow(newTop, rangeLeft, rangeRight);
			rangeBtm = _mergeMatrix.getBottomConnectedRow(newBtm, rangeLeft, rangeRight);
		}

		
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		JSONObject data = spreadsheetCtrl.getRangeAttrs(sheet, 
				header, SpreadsheetCtrl.CellAttribute.ALL, rangeLeft, rangeTop, rangeRight, rangeBtm);
		data.put("dir", "jump");
		json.put("data", data);
		
		_lastleft = left;
		_lastright = right;
		_lasttop = top;
		_lastbottom = bottom;
		
		
		// prepare top frozen cell
		int fzr = _spreadsheet.getRowfreeze();
		if (fzr > -1) {
			
			JSONObject topFrozen = new JSONObject();
			json.put("topFrozen", topFrozen);
			
			topFrozen.put("type", "jump");
			topFrozen.put("dir", "jump");
			topFrozen.put("left", left);
			topFrozen.put("top", 0);
			topFrozen.put("width", w);
			topFrozen.put("height", fzr + 1);
			
			ce = left + w;
			
			data = spreadsheetCtrl.getRangeAttrs(sheet, 
					header, SpreadsheetCtrl.CellAttribute.ALL, rangeLeft, 0, rangeRight, fzr);
			topFrozen.put("data", data);
		}
		
		
		//prepare left frozen cell
		int fzc = _spreadsheet.getColumnfreeze();
		if (fzc > -1) {
			JSONObject leftFrozen = new JSONObject();
			json.put("leftFrozen", leftFrozen);
			
			leftFrozen.put("type", "jump");
			leftFrozen.put("dir", "jump");
			leftFrozen.put("left", 0);
			leftFrozen.put("top", top);
			leftFrozen.put("width", fzc + 1);
			leftFrozen.put("height", h);
			
			re = top + h;
			
			data = spreadsheetCtrl.getRangeAttrs(sheet, 
					header, SpreadsheetCtrl.CellAttribute.ALL, 0, rangeTop, fzc, rangeBtm);
			leftFrozen.put("data", data);
		}
		return json.toString();
	}
	
	private String jump(String dir,Spreadsheet spreadsheet,String sheetId, Worksheet sheet, String type,
			int dpWidth, int dpHeight, int viewWidth, int viewHeight,
			int blockLeft, int blockTop, int blockRight, int blockBottom,
			int col, int row, 
			int rangeLeft, int rangeTop, int rangeRight,int rangeBottom) {
		
		int left;
		int right;
		int top;
		int bottom;
		
		
		if (dir.indexOf("E") >= 0) {
			right = col + 1;
			left = _colHelper.getCellIndex(_colHelper.getStartPixel(col) - viewWidth);
			// w = col - left + 2;//load more;

			if (right > spreadsheet.getMaxcolumns() - 1) {
				// w = spreadsheet.getMaxcolumn()-left;
				right = spreadsheet.getMaxcolumns() - 1;
			}
		} else if (dir.indexOf("W") >= 0) {
			left = col <= 0 ? 0 : col - 1;
			right = _colHelper.getCellIndex(_colHelper.getStartPixel(col)
					+ viewWidth);// end cell index

			if (right > spreadsheet.getMaxcolumns() - 1) {
				// w = spreadsheet.getMaxcolumn()-left;
				right = spreadsheet.getMaxcolumns() - 1;
			}
		} else {
			left = blockLeft;// rangeLeft;
			right = blockRight;// rangeRight;
		}
		
		if (dir.indexOf("S") >= 0) {
			bottom = row + 1;
			top = _rowHelper.getCellIndex(_rowHelper.getStartPixel(row)	- viewHeight);

			if (bottom > spreadsheet.getMaxrows() - 1) {
				bottom = spreadsheet.getMaxrows() - 1;
			}
		} else if (dir.indexOf("N") >= 0) {
			top = row <= 0 ? 0 : row - 1;
			bottom = _rowHelper.getCellIndex(_rowHelper.getStartPixel(row) + viewHeight);// end cell index

			if (bottom > spreadsheet.getMaxrows() - 1) {
				bottom = spreadsheet.getMaxrows() - 1;
			}
		} else {
			top = blockTop;// rangeTop;
			bottom = blockBottom;// rangeBottom;
		}
		
		return jumpResult(sheet,left,top,right,bottom);
	}
	
	private LoadResult loadEast(Worksheet sheet,String type, 
			int blockLeft,int blockTop,int blockRight, int blockBottom,
			int fetchWidth, int rangeWidth, int rangeTopHeight, int rangeBtmHeight) {

		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", fetchWidth);
		json.put("height", blockBottom - blockTop + 1); //the range of height to generate DOM
		
		//append row
		int cs = blockRight + 1;
		int ce = cs + fetchWidth;
		json.put("top", blockTop);
		json.put("left", cs);
		
		int rangeTop = rangeTopHeight > 0 ? blockTop - rangeTopHeight + 1 : blockTop;
		int rangeRight = Math.min(rangeWidth > fetchWidth ? cs + rangeWidth - 1 : ce - 1, _spreadsheet.getMaxcolumns() - 1); 
		int rangeBottom = rangeBtmHeight < 0 ? blockBottom : blockBottom + rangeBtmHeight - 1;
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		JSONObject data = spreadsheetCtrl.getRangeAttrs(sheet, _hidecolhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.COLUMN, 
				SpreadsheetCtrl.CellAttribute.ALL, cs, rangeTop, rangeRight, rangeBottom);
		data.put("dir", "east");
		json.put("data", data);
	
		_lastleft = blockLeft;
		_lastright = ce - 1;
		_lasttop = blockTop;
		_lastbottom = blockBottom;

		//process frozen row data
		int fzr = _spreadsheet.getRowfreeze();
		if (fzr > -1) {
			
			JSONObject topFrozen = new JSONObject();
			json.put("topFrozen", topFrozen);
			
			topFrozen.put("type", "neighbor");
			topFrozen.put("width", fetchWidth);
			topFrozen.put("height", fzr + 1);
			
			data = spreadsheetCtrl.getRangeAttrs(sheet, _hiderowhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.ROW, SpreadsheetCtrl.CellAttribute.ALL, 
					cs, 0, rangeRight, fzr);
			data.put("dir", "east");
			topFrozen.put("data", data);
		}
		return new LoadResult(rangeRight, json);
	}
	
	private LoadResult loadWest(Worksheet sheet,String type,
			int blockLeft,int blockTop,int blockRight, int blockBottom,
			int fetchWidth, int rangeWidth, int rangeTopHeight, int rangeBtmHeight) {
		
		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", fetchWidth);// increased cell size
		json.put("height", blockBottom - blockTop + 1);// increased cell size
		
		// append row
		int cs = blockLeft - 1;
		int ce = cs - fetchWidth;
		json.put("top", blockTop);
		json.put("left", ce + 1);
		
		int rangeTop = rangeTopHeight > 0 ? blockTop - rangeTopHeight + 1 : blockTop;
		int rangeLeft = rangeWidth > fetchWidth ? blockLeft - rangeWidth - 1: ce + 1;
		if (rangeLeft < 0)
			rangeLeft = 0;
		int rangeBottom = rangeBtmHeight < 0 ? blockBottom : blockBottom + rangeBtmHeight - 1;
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		JSONObject data = spreadsheetCtrl.getRangeAttrs(sheet, _hidecolhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.COLUMN, 
				SpreadsheetCtrl.CellAttribute.ALL, rangeLeft, rangeTop, cs, rangeBottom);
		data.put("dir", "west");
		json.put("data", data);
		
		_lastleft = ce+1;
		_lastright = blockRight;
		_lasttop = blockTop;
		_lastbottom = blockBottom;
		
		// process frozen row data
		int fzr = _spreadsheet.getRowfreeze();
		if (fzr > -1) {
			
			JSONObject topFrozen = new JSONObject();
			json.put("topFrozen", topFrozen);
			
			topFrozen.put("type", "neighbor");
			topFrozen.put("width", fetchWidth);
			topFrozen.put("height", fzr + 1);
			
			data = spreadsheetCtrl.getRangeAttrs(sheet, _hiderowhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.ROW, SpreadsheetCtrl.CellAttribute.ALL, 
					rangeLeft, 0, cs, fzr);
			data.put("dir", "west");
			topFrozen.put("data", data);
		}
		return new LoadResult(rangeLeft, json);
	}
	
	private String loadSouth(Worksheet sheet, String type, 
			int blockLeft,int blockTop, int blockRight, int blockBottom, int fetchHeight, int rangeLeft, int rangeRight, int cacheRangeHeight) {
		
		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", blockRight - blockLeft + 1);
		json.put("height", fetchHeight);

		int rs = blockBottom + 1;
		int re = rs + fetchHeight;
		json.put("top", rs);
		json.put("left", blockLeft);
		
		int rangeBottom = Math.min(cacheRangeHeight > fetchHeight ? rs + cacheRangeHeight - 1 : re - 1, _spreadsheet.getMaxrows() - 1);
		rangeLeft = rangeLeft > 0 && rangeLeft < blockLeft ? rangeLeft : blockLeft;
		rangeRight = Math.min(Math.max(blockRight, rangeRight), _spreadsheet.getMaxcolumns() - 1);
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		JSONObject data = spreadsheetCtrl.getRangeAttrs(sheet, _hidecolhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.ROW, SpreadsheetCtrl.CellAttribute.ALL, 
				rangeLeft, rs, rangeRight, rangeBottom);
		data.put("dir", "south");
		json.put("data", data);

		_lastleft = blockLeft;
		_lastright = blockRight;
		_lasttop = blockTop;
		_lastbottom = re-1;
		
		// process frozen left
		int fzc = _spreadsheet.getColumnfreeze();
		if (fzc > -1) {
			
			JSONObject leftFrozen = new JSONObject();
			json.put("leftFrozen", leftFrozen);
			
			leftFrozen.put("type", "neighbor");
			leftFrozen.put("width", fzc + 1);
			leftFrozen.put("height", fetchHeight);
			data = spreadsheetCtrl.getRangeAttrs(sheet, _hidecolhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.COLUMN, SpreadsheetCtrl.CellAttribute.ALL,
					0, rs, fzc, rangeBottom);
			data.put("dir", "south");
			leftFrozen.put("data", data);
		}

		return json.toString();
	}
	private String loadNorth(Worksheet sheet,String type, 
			int blockLeft, int blockTop, int blockRight, int blockBottom,
			int fetchHeight, int rangeLeft, int rangeRight, int cacheRangeHeight) {

		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", blockRight - blockLeft + 1);
		json.put("height", fetchHeight);
		
		int rs = blockTop - 1;
		int re = rs - fetchHeight;
		json.put("top", re + 1);
		json.put("left", blockLeft);
		
		int rangeTop = cacheRangeHeight > fetchHeight ? rs - cacheRangeHeight - 1 : re + 1;
		rangeLeft = rangeLeft > 0 && rangeLeft < blockLeft ? rangeLeft : blockLeft;
		rangeRight = Math.min(Math.max(blockRight, rangeRight), _spreadsheet.getMaxcolumns() - 1);
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		JSONObject data = spreadsheetCtrl.getRangeAttrs(sheet, _hidecolhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.ROW, SpreadsheetCtrl.CellAttribute.ALL, 
				rangeLeft, rangeTop, rangeRight, rs);
		data.put("dir", "north");
		json.put("data", data);
		
		_lastleft = blockLeft;
		_lastright = blockRight;
		_lasttop = re + 1;
		_lastbottom = blockBottom;
		
		// process frozen left
		int frc = _spreadsheet.getColumnfreeze();
		if (frc > -1) {
			
			JSONObject leftFrozen = new JSONObject();
			json.put("leftFrozen", leftFrozen);
			
			leftFrozen.put("type", "neighbor");
			leftFrozen.put("width", frc + 1);
			leftFrozen.put("height", fetchHeight);
			data = spreadsheetCtrl.getRangeAttrs(sheet, _hidecolhead ? SpreadsheetCtrl.Header.NONE : SpreadsheetCtrl.Header.COLUMN, SpreadsheetCtrl.CellAttribute.ALL,
					0, rangeTop, frc, rs);
			data.put("dir", "north");
			leftFrozen.put("data", data);
		}
		return json.toString();
	}
	
	private class LoadResult {
		int index;
		JSONObject json;
		
		LoadResult(int loadSize, JSONObject json) {
			this.index = loadSize;
			this.json = json;
		}
	}
}