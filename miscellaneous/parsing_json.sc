import io.github.morgaroth.navigator_import.android.utils.GPXGetProtocol

val j ="""{ "waypoints": [{ "name": "50.0682883,19.9036728", "lat": 50.06834030151367, "lon": 19.903310775756836 }, { "name": "Parkowa, Kraków", "lat": 50.04098129272461, "lon": 19.95100975036621 }, { "name": "Przemysłowa 12, 33-332 Kraków", "lat": 50.04874038696289, "lon": 19.960060119628906 }] }"""

val parser = new GPXGetProtocol {}
import parser._

val p = j.parseMyGPX
p.right.get


