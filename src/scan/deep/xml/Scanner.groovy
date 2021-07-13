package scan.deep.xml

import groovy.json.JsonSlurper

class Scanner {
    HashMap<String, ArrayList<String>> xmlElement = new HashMap<String, ArrayList<String>>()
    HashMap<String, ArrayList<String>> mplLog = new HashMap<String, ArrayList<String>>()

    Map scan(byte[] json, String xml, String interfaceId) throws Exception {
        Map jsonParser = new JsonSlurper().parseText(new String(json))
        ArrayList<String> vocabulary = new ArrayList<String>()
        Node root

        try {
            root = new XmlParser().parseText(xml)
            jsonParser.get('config').each {
                Map config ->
                    if (config.get('interface') == interfaceId) {
                        vocabulary = config.get('element') as ArrayList<String>
                    }
            }
            if (vocabulary.size() == 0) {
                throw new Exception("No configuration found for the interface ${interfaceId}")
            }
            this.deepScan(root)
            this.getXmlElement().each {
                if (vocabulary.contains(it.getKey())) {
                    this.getMplLog().put(it.getKey(), it.getValue())
                }
            }
            return this.getMplLog()
        }
        catch (Exception exception) {
            throw exception
        }
    }

    void deepScan(Node root) {
        root.children().each {
            Node node ->
                ArrayList<String> xmlElementItem
                if (node.children().get(0) instanceof Node) {
                    this.deepScan(node)
                } else {
                    if (this.getXmlElement().containsKey(node.name())) {
                        xmlElementItem = this.getXmlElement().get(node.name())
                    } else {
                        xmlElementItem = new ArrayList<String>()
                    }
                    if (!xmlElementItem.contains(node.text())) {
                        xmlElementItem.add(node.text())
                        this.getXmlElement().put(node.name() as String, xmlElementItem)
                    }
                }
        }
    }
}
