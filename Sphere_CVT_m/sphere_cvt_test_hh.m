function sphere_cvt_test_hh ( )

%*****************************************************************************80
%
%% SPHERE_CVT_TEST_HH tests the SPHERE_CVT library.
%
%  Licensing:
%
%    This code is distributed under the GNU LGPL license. 
%
%  Modified:
%
%    03 May 2010
%
%  Author:
%
%    John Burkardt
%
  timestamp ( );
  fprintf ( 1, '\n' );
  fprintf ( 1, 'SPHERE_CVT_TEST_HH\n' );
  fprintf ( 1, '  MATLAB version:\n' );
  fprintf ( 1, '  Test the SPHERE_CVT library.\n' );

  sphere_cvt_test01_hh ( );
%
%  Terminate.
%
  fprintf ( 1, '\n' );
  fprintf ( 1, 'SPHERE_CVT_TEST_HH:\n' );
  fprintf ( 1, '  Normal end of execution.\n' );
  fprintf ( 1, '\n' );
  timestamp ( );

  return
end
function sphere_cvt_test01_hh ( )

%*****************************************************************************80
%
%% SPHERE_CVT_TEST01 demonstrates the computation of a CVT on the unit sphere.
%
%  Licensing:
%
%    This code is distributed under the GNU LGPL license. 
%
%  Modified:
%
%    03 May 2010
%
%  Author:
%
%    John Burkardt
%
  fprintf ( 1, '\n' );
  fprintf ( 1, 'SPHERE_CVT_TEST01_HH\n' );
  fprintf ( 1, '  Demonstrate the iterative computation of a CVT\n' );
  fprintf ( 1, '  (Centroidal Voronoi Tessellation) on the unit sphere.\n' );
%
%  Choose a random set of points on the unit sphere.
%
  n = 5; % 100;
  seed = 123456789;
  [ d_xyz, seed ] = uniform_on_sphere01_map ( 3, n, seed );
  r8mat_transpose_print_hh ( 3, n, d_xyz, '  Initial points:' );

%  figure ( 1 )
%  sphere_voronoi_plot ( n, xyz )

  for i = 1 : 200

    centroid = sphere_cvt_step ( n, d_xyz );

    d_xyz(1:3,1:n) = centroid(1:3,1:n);

  end

  r8mat_transpose_print_hh ( 3, n, d_xyz, '  Final points:' );
  
%
%  Compute the Delaunay triangulation.
%
  [ face_num, face ] = sphere_delaunay ( n, d_xyz );
  i4mat_transpose_print ( 3, face_num, face, '  Delaunay vertices:' );
%
%  Compute the Delaunay triangle neighbor array.
%
  face_neighbors = triangulation_neighbor_triangles ( 3, face_num, face );
  i4mat_transpose_print ( 3, face_num, face_neighbors, '  Delaunay neighbors:' );
%
%  For each Delaunay triangle, compute the normal vector, to get the 
%  Voronoi vertex.
%
  v_xyz = voronoi_vertices ( n, d_xyz, face_num, face );
  r8mat_transpose_print_hh ( 3, face_num, v_xyz , '  Voronoi vertices' );
%
%  Compute the order of each Voronoi polygon.
%
  order = voronoi_order ( n, face_num, face );
  i4vec_print ( n, order, '  Voronoi orders:' );
%
%  Compute the Voronoi vertex lists that define the Voronoi polygons.
%
  [ first, list ] = voronoi_polygons ( n, face_num, face );
  list_num = 2 * face_num;
  i4list_print ( n, first, list_num, list, '  Voronoi polygons:' );
%
%  Compute the areas of the Voronoi polygons.
%
  v_num = face_num;
  area = voronoi_areas ( n, first, list_num, list, d_xyz, v_num, v_xyz );
  r8vec_print ( n, area, '  Voronoi areas:' );
%
%  Compute the centroids of the polygons.
%
  centroid = voronoi_centroids ( n, first, list_num, list, d_xyz, v_num, v_xyz );
  r8mat_transpose_print_hh ( 3, n, centroid, '  Voronoi centroids' );
  
  
%
%  Plot the final data.
%
  figure ( 2 )
  sphere_voronoi_plot ( n, d_xyz );

  return
end
