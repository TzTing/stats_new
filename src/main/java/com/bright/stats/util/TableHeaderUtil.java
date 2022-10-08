package com.bright.stats.util;

import com.bright.stats.pojo.model.TableHeader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/6/27 9:41
 * @Description
 */
public class TableHeaderUtil {

    private static List<TableHeader> getRootNodes(List<TableHeader> nodes){
        List<TableHeader> rootNodes = new ArrayList<>();
        for (TableHeader n : nodes) {
            if (isRootNode(nodes, n)) {
                rootNodes.add(n);
            }
        }
        return rootNodes;
    }

    private static Boolean isRootNode(List<TableHeader> nodes, TableHeader headerTree){
        boolean isRootNode = true;
        for (TableHeader n : nodes) {
            if(headerTree.getPid().equalsIgnoreCase(n.getId())){
                isRootNode = false;
                break;
            }
        }
        return isRootNode;
    }

    private static List<TableHeader> getChildNodes(List<TableHeader> nodes, TableHeader pnode) {
        List<TableHeader> childNodes = new ArrayList<>();
        for(TableHeader tree : nodes){
            if(pnode.getId().equalsIgnoreCase(tree.getPid())){
                childNodes.add(tree);
            }
        }

        return childNodes;
    }

    private static void buildChildNodes(List<TableHeader> nodes, TableHeader node) {
        List<TableHeader> childrenList = getChildNodes(nodes, node);
        Set<TableHeader> tableHeaderSet = new TreeSet<>((o1, o2) -> o1.getId().compareTo(o2.getId()));
        tableHeaderSet.addAll(childrenList);
        List<TableHeader> children = tableHeaderSet.stream().collect(Collectors.toList());
        Collections.sort(children, Comparator.comparing(TableHeader::getSort));
        if (!children.isEmpty()) {
            for (TableHeader child : children) {
                buildChildNodes(nodes, child);
            }
            node.setChildren(children);
        }
    }

    public static List<TableHeader> buildTree(List<TableHeader> nodes) {
        List<TableHeader> treeNodes = new ArrayList<>();
        List<TableHeader> rootNodes = getRootNodes(nodes);
        for (TableHeader rootNode : rootNodes) {
            buildChildNodes(nodes, rootNode);
            treeNodes.add(rootNode);
        }
        return treeNodes;
    }
}
