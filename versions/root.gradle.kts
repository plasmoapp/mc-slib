plugins {
    id("gg.essential.multi-version.root")
}

group = "$group.versions-root"

preprocess {
    strictExtraMappings.set(true)

    val fabric12105 = createNode("1.21.5-fabric", 12105, "official")
    val neoForge12105 = createNode("1.21.5-neoforge", 12105, "official")

    val fabric12102 = createNode("1.21.2-fabric", 12102, "official")
    val neoForge12102 = createNode("1.21.2-neoforge", 12102, "official")

    val fabric12100 = createNode("1.21-fabric", 12100, "official")
    val neoForge12100 = createNode("1.21-neoforge", 12100, "official")
    val forge12100 = createNode("1.21-forge", 12100, "official")

    val fabric12006 = createNode("1.20.6-fabric", 12006, "official")

    val forge12004 = createNode("1.20.4-forge", 12004, "official")
    val fabric12004 = createNode("1.20.4-fabric", 12004, "official")

    val forge12002 = createNode("1.20.2-forge", 12002, "official")
    val fabric12002 = createNode("1.20.2-fabric", 12002, "official")

    val forge12001 = createNode("1.20.1-forge", 12001, "official")
    val fabric12001 = createNode("1.20.1-fabric", 12001, "official")

    val fabric11903 = createNode("1.19.3-fabric", 11903, "official")
    val forge11903 = createNode("1.19.3-forge", 11903, "official")

    val fabric11902 = createNode("1.19.2-fabric", 11902, "official")
    val forge11902 = createNode("1.19.2-forge", 11902, "official")

    val fabric11802 = createNode("1.18.2-fabric", 11802, "official")
    val forge11802 = createNode("1.18.2-forge", 11802, "official")

    val fabric11701 = createNode("1.17.1-fabric", 11701, "official")
    val forge11701 = createNode("1.17.1-forge", 11701, "official")

    val fabric11605 = createNode("1.16.5-fabric", 11605, "official")
    val forge11605 = createNode("1.16.5-forge", 11605, "official")

    fabric12105.link(fabric12102)
    neoForge12105.link(neoForge12102)

    fabric12102.link(fabric12100)
    neoForge12102.link(neoForge12100)

    neoForge12100.link(fabric12006)
    fabric12100.link(fabric12006)
    forge12100.link(forge12004)

    fabric12006.link(fabric12004)

    fabric12004.link(fabric12002)
    forge12004.link(forge12002)

    fabric12002.link(fabric12001, file("1.20.2-1.20.1.txt"))
    forge12002.link(forge12001, file("1.20.2-1.20.1.txt"))

    fabric12001.link(fabric11903)
    forge12001.link(forge11903)

    forge11903.link(fabric11903)

    fabric11902.link(fabric11903)
    forge11902.link(forge11903)

    fabric11802.link(fabric11902)
    forge11802.link(forge11902)

    fabric11701.link(fabric11802)
    forge11701.link(forge11802, file("1.17.1-1.18.2-forge.txt"))

    fabric11605.link(fabric11701)
    forge11605.link(forge11701, file("1.16.5-1.17.1-forge.txt"))
}
