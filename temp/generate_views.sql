drop table available_flights;
create view available_flights(flightno,dep_date, src,dst,dep_time,arr_time,fare,seats,
	price) as
select f.flightno, sf.dep_date, f.src, f.dst, f.dep_time+(trunc(sf.dep_date)-trunc(f.dep_time)),
 f.dep_time+(trunc(sf.dep_date)-trunc(f.dep_time))+(f.est_dur/60+a2.tzone-a1.tzone)/24,
       fa.fare, fa.limit-count(tno), fa.price
from flights f, flight_fares fa, sch_flights sf, bookings b, airports a1, airports a2
where f.flightno=sf.flightno and f.flightno=fa.flightno and f.src=a1.acode and
f.dst=a2.acode and fa.flightno=b.flightno(+) and fa.fare=b.fare(+) and
sf.dep_date=b.dep_date(+)
group by f.flightno, sf.dep_date, f.src, f.dst, f.dep_time, f.est_dur,a2.tzone,
a1.tzone, fa.fare, fa.limit, fa.price
having fa.limit-count(tno) > 0;

select flightno, dep_date, src, dst, to_char(dep_time,'HH24:MI'),
       to_char(arr_time, 'HH24:MI'), fare, seats, price
from available_flights
order by dep_date;
