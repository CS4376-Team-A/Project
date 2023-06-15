package SearchEngine.Repositories;

import SearchEngine.Models.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IndexRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveIndex(Index index) {
        String sql = "INSERT INTO indexes (url, title, description, keywords) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, index.url, index.title, index.description, index.keywords);
    }

    public boolean doesUrlExist(String url) {
        String sql = "SELECT COUNT(*) FROM indexes WHERE url = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, url);
        return count > 0;
    }
}
