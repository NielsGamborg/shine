/**
 * 
 */
package uk.bl.wa.shine;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

import play.Logger;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Query {

	public static final String FACET_SORT_INDEX = "index";
	public static final String FACET_SORT_COUNT = "count";
	
	public String query;
	
	public Map<String,List<String>> filters;
	
	public QueryResponse res;
	
	public void parseParams( Map<String,List<String>> params ) {
		filters = new HashMap<String, List<String>>();
		for( String param : params.keySet() ) {
			if( param.startsWith("facet.in.")) {
			    filters.put(param.replace("facet.in.", ""), params.get(param));
			} else if( param.startsWith("facet.out.")) {
			    filters.put("-"+param.replace("facet.out.", ""), params.get(param));
			} else if( param.equals("facet.sort") ) {
			    filters.put(param, params.get(param));
			}
		}
		Logger.info("filters: " + filters);
	}
	
	public String getCheckedInString(String facet_name, String value ) {
		for( String fc : filters.keySet() ) {
			if( fc.equals(facet_name) && filters.get(fc).contains("\""+value+"\"")) {
				return "checked=''";
			}
		}
		return "";
	}
	
	public String getCheckedOutString(String facet_name, String value ) {
		return this.getCheckedInString("-"+facet_name, value);
	}
	
	public String getParamsPlusFilter(String facet_name, String facet_value) {
		String qp = "";
//		Logger.info("---- ----");
		for( String key : res.getFacetQuery().keySet() ) {
//			Logger.info(key+">"+res.getFacetQuery().get(key));
		}
		for( FacetField fc: res.getLimitingFacets() ) {
//			Logger.info("LF: "+fc);
		}
		for( FacetField fc : this.res.getFacetFields() ) {
//			Logger.info("FF: "+fc);
			if( fc.getName().equals(facet_name) ) {
				
			}
		}
		return qp;
	}

	// Formatters	

	// Allow for pretty formatting of facet values:
	public String formatFacet( FacetField fc, Count f ) {
		if( "content_first_bytes".equals(fc.getName()) )
			return this.formatHexString(f.getName());
		if( "content_ffb".equals(fc.getName()) )
			return this.formatHexString(f.getName());
		return f.getName();
	}

	// Format numbers with commas:
	public String formatNumber( long number ) {
		NumberFormat numberFormat = new DecimalFormat("#,###");
		return numberFormat.format(number);
	}
	
	// Hex to string:
	// TODO Moving the HTML encoding (below) into templates.
	public String formatHexString( String hex ) {
		hex = hex.replaceAll(" ", "");
		try {
			byte[] bytes = Hex.decodeHex(hex.toCharArray());
			hex = this.partialHexDecode(bytes);
		} catch (DecoderException e) {
			Logger.error("Hex decode failed: "+e);
		} catch (UnsupportedEncodingException e) {
			Logger.error("Hex to UTF-8 recoding failed: "+e);
		}
		return hex;
	}
	
	public String getFacetValue(String facet_name) {
		if (filters.get(facet_name) != null) {
			return filters.get(facet_name).get(0);
		}
		return "";
	}
	
	public String getCheckedFacet(String facet_name) {
		if (StringUtils.isNotBlank(this.getFacetValue(facet_name))) {
			return "checked=''";
		}
		return "";
	}
	
	public String getFacetSortValue(String facet_name) {
		// only return the correct facet sort values
		if (facet_name.equals("facet.sort")) {
			return getFacetValue(facet_name);
		}
		return "";
	}
	
	public String getFacetsAsParamValues() {
		StringBuilder parameters = new StringBuilder("");
		for (FacetField facetField : res.getFacetFields()) {
			for (Count count : facetField.getValues()) {
				String facet = facetField.getName() + "=\"" + count.getName() + "\"";
				if (StringUtils.isNotBlank(this.getCheckedInString(facetField.getName(),count.getName()))) {
					String in = "&facet.in."; 
					parameters.append(in).append(facet);
				} else if (StringUtils.isNotBlank(this.getCheckedOutString(facetField.getName(),count.getName()))) {
					String out = "&facet.out";
					parameters.append(out).append(facet);
				}
			}
		 }
		String facetSort = "facet.sort";
		String checked = getCheckedFacet(facetSort);
		if (StringUtils.isNotBlank(checked)) {
			String sortValue = getFacetSortValue(facetSort);
			parameters.append("&").append(facetSort).append("=").append(sortValue);
		}
		Logger.info(parameters.toString());
		return parameters.toString();
	}
	
	private String partialHexDecode( byte[] bytes ) throws UnsupportedEncodingException {
		String myString = new String( bytes, "ASCII");
		StringBuilder newString = new StringBuilder(myString.length());
		for (int offset = 0; offset < myString.length();)
		{
		    int codePoint = myString.codePointAt(offset);
		    offset += Character.charCount(codePoint);

		    // Replace invisible control characters and unused code points
		    switch (Character.getType(codePoint))
		    {
		        case Character.CONTROL:     // \p{Cc}
		        case Character.FORMAT:      // \p{Cf}
		        case Character.PRIVATE_USE: // \p{Co}
		        case Character.SURROGATE:   // \p{Cs}
		        case Character.UNASSIGNED:  // \p{Cn}
		            newString.append("<i class=\"hex\">");
		            newString.append(Hex.encodeHexString(new byte[] {Byte.valueOf((byte) codePoint) } ));
		            newString.append("</i>");
		            break;
		        default:
		            newString.append("<span class=\"lit\">");
		            newString.append(Character.toChars(codePoint));
		            newString.append("</span>");
		            break;
		    }
		}
		return newString.toString();
	}
}
