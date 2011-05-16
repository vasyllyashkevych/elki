package de.lmu.ifi.dbs.elki.database.datastore.memory;

import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.RangeIDMap;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDataStore;
import de.lmu.ifi.dbs.elki.database.datastore.WritableRecordStore;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRange;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;

/**
 * Simple factory class that will store all data in memory using object arrays or hashmaps.
 * 
 * Hints are currently not used by this implementation, since everything is in-memory.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.stereotype factory
 * @apiviz.uses ArrayStore oneway - - «create»
 * @apiviz.uses ArrayRecordStore oneway - - «create»
 * @apiviz.uses MapStore oneway - - «create»
 * @apiviz.uses MapRecordStore oneway - - «create»
 */
public class MemoryDataStoreFactory implements DataStoreFactory {
  @Override
  public <T> WritableDataStore<T> makeStorage(DBIDs ids, @SuppressWarnings("unused") int hints, @SuppressWarnings("unused") Class<? super T> dataclass) {
    if (ids instanceof DBIDRange) {
      DBIDRange range = (DBIDRange) ids;
      Object[] data = new Object[range.size()];
      return new ArrayStore<T>(data, new RangeIDMap(range));
    } else {
      return new MapStore<T>();
    }
  }

  @Override
  public WritableRecordStore makeRecordStorage(DBIDs ids, @SuppressWarnings("unused") int hints, Class<?>... dataclasses) {
    if (ids instanceof DBIDRange) {
      DBIDRange range = (DBIDRange) ids;
      Object[][] data = new Object[range.size()][dataclasses.length];
      return new ArrayRecordStore(data, new RangeIDMap(range));
    } else {
      return new MapRecordStore(dataclasses.length);
    }
  }
}