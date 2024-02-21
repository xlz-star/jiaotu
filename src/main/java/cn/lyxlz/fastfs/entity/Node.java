package cn.lyxlz.fastfs.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Objects;

@Data
@Accessors(chain = true)
public class Node implements Comparable<Node>, Serializable {

    private static final long serialVersionUID = -4839355924068909330L;
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 剩余空间大小
     */
    private Long free;
    /**
     * 总空间大小
     */
    private Long total;

    @Override
    public int compareTo(Node o) {
        return this.free.compareTo(o.free);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return Objects.equals(nodeName, node.nodeName);
    }

    @Override
    public int hashCode() {
        return nodeName != null ? nodeName.hashCode() : 0;
    }
}
