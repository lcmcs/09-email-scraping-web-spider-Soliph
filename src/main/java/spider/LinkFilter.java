package spider;

import java.util.Collection;

public interface LinkFilter {

    Collection<String> filterLinks(Collection<String> links);

}
