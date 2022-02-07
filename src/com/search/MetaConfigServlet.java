package com.search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MetaConfigServlet
 */
public class MetaConfigServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MetaConfigServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			
			String engineToDelete = req.getParameter("delete");
			String engineToToggle = req.getParameter("toggle");
			String engineToCreate = req.getParameter("engine_url");
			
			if (engineToDelete != null) {
				MetaConf.deleteEngine(Integer.parseInt(engineToDelete));
			} else if (engineToToggle != null) {
				MetaConf.toggleEngine(Integer.parseInt(engineToToggle));
			} else if (engineToCreate != null) {
				MetaConf.addEngine(engineToCreate);
			}
			
			Collection.setActiveEngines(MetaConf.getActiveEngines());
			Collection.updateCollectionCount();
			Collection.updateAvgCw();
			
			List<Engine> engines = MetaConf.getConf();
			req.setAttribute("engines", engines);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			req.setAttribute("error", e.getMessage());
			e.printStackTrace();
		}

		RequestDispatcher rd = req.getRequestDispatcher("meta_config.jsp");
		rd.forward(req, res);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, res);
	}

}
