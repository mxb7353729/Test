package com.evialab.gisserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

/**
 * Servlet implementation class StaticCach
 */
public class StaticCach extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	public static  String StaticachPath = "d:/staticcach/";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StaticCach() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		//从文件读�?
		 
		
		byte[] img = readfile(StaticachPath+RequestUtil.getParameter(request, "f"));
		if(img != null){
			
			response.setContentType("image/png");
			response.setContentLength(img.length);
			response.getOutputStream().write(img);
		}else
		{
			response.sendError(404);
		}
		
	}

	
	private byte[] readfile( String file) {
		try {
		 
			File f = new File(file);
			FileInputStream fis = new FileInputStream(f);
			byte[] by = new byte[(int) f.length()];
			fis.read(by);
			fis.close();

			return by;
		} catch (Exception e) {
			 
			return null;
		}
	}
	
}
