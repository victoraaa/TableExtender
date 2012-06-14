TableExtender
=============

Given a website with a table and an initial input table with some rows from that table, extends the table with data from the site.

--
Dependencies

The libs you need directly for this are under lib. You need to add them to your build path.
You need to download the project RoadRunner from http://www.dia.uniroma3.it/db/roadRunner/ and 
import the project to your build path. You should make ONE modification to its code: on the 'main' method of the class roadrunner.Shell, 
put "Shell.urls.clear();" as the first line.