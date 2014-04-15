package com.vitco.export;

import com.vitco.export.container.TexTriangleManager;
import com.vitco.manager.error.ErrorHandlerInterface;
import com.vitco.settings.VitcoSettings;
import com.vitco.util.misc.DateTools;
import com.vitco.util.xml.XmlFile;

import java.io.File;

/**
 * Export data to COLLADA file ( with optional settings )
 */
public class ColladaFileExporter {
    // contains the data that will be used to write this collada file
    private final ExportDataManager exportDataManager;

    // the xml Collada file that we're building
    private final XmlFile xmlFile = new XmlFile("COLLADA");

    // constructor
    public ColladaFileExporter(ExportDataManager exportDataManager) {
        this.exportDataManager = exportDataManager;
        initXmlFile();
        writeTextures();
        writeCoordinates();
    }

    private void writeTextures() {
        // write the image to the library
        xmlFile.resetTopNode("library_images/image[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=file1-image",
                "name=file1-image"
        });
        xmlFile.addTextContent("init_from", "tmp.png");

        // create the texture object
        xmlFile.resetTopNode("library_visual_scenes/visual_scene/node[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=PlaneTEX",
                "name=PlaneTEX",
                "type=NODE"
        });
        xmlFile.addAttrAndTextContent("translate", new String[]{"sid=location"},
                "0 0 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationZ"}, "0 0 1 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationY"}, "0 1 0 0");
        xmlFile.addAttrAndTextContent("rotate[-1]", new String[]{"sid=rotationX"}, "1 0 0 0");
        // scale the object down
        xmlFile.addAttrAndTextContent("scale", new String[]{"sid=scale"}, "0.05 0.05 0.05");

        // add the material to the object
        xmlFile.setTopNode("instance_geometry[-1]");
        xmlFile.addAttributes("", new String[] {"url=#Plane-tex-mesh"});
        xmlFile.setTopNode("bind_material/technique_common/instance_material[-1]");
        xmlFile.addAttributes("", new String[] {
                "symbol=lambert1-material",
                "target=#lambert1-material"
        });
        // add the uv mapping
        xmlFile.addAttributes("bind_vertex_input", new String[] {
                "semantic=TEX0",
                "input_semantic=TEXCOORD",
                "input_set=0"
        });

        // add the material
        xmlFile.resetTopNode("library_materials/material[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=lambert1-material",
                "name=lambert1"
        });
        xmlFile.addAttributes("instance_effect", new String[] {
                "url=#lambert1-fx"
        });

        // write the image effect
        xmlFile.resetTopNode("library_effects/effect[-1]");
        xmlFile.addAttributes("", new String[]{
                "id=lambert1-fx"
        });
        xmlFile.setTopNode("profile_COMMON");
        // ----
        xmlFile.setTopNode("newparam[-1]");
        xmlFile.addAttributes("", new String[]{"sid=file1-surface"});
        xmlFile.addAttributes("surface", new String[]{"type=2D"});
        xmlFile.addTextContent("surface/init_from", "file1-image");
        // ----
        xmlFile.goUp();
        xmlFile.setTopNode("newparam[-1]");
        xmlFile.addAttributes("", new String[]{"sid=file1-sampler"});
        xmlFile.addTextContent("sampler2D/source", "file1-surface");
        // ----
        xmlFile.goUp();
        xmlFile.setTopNode("technique[-1]");
        xmlFile.addAttributes("", new String[]{"sid=common"});
        xmlFile.addTextContent("lambert/emission/color", "0 0 0 1");
        xmlFile.addTextContent("lambert/ambient/color", "0 0 0 1");
        xmlFile.addAttributes("lambert/diffuse/texture", new String[]{
                "texture=file1-sampler",
                "texcoord=TEX0"
        });
    }

    // write the coordinates
    private void writeCoordinates() {
        TexTriangleManager texTriangleManager = exportDataManager.getTriangleManager();

        // reset top node
        xmlFile.resetTopNode("library_geometries/geometry[-1]");
        xmlFile.addAttributes("", new String[] {
                "id=Plane-tex-mesh",
                "name=Plane-tex"
        });
        xmlFile.setTopNode("mesh");

        // Plane-tex-mesh-positions
        xmlFile.addAttributes("source[-1]", new String[] {
                "id=Plane-tex-mesh-positions"
        });
        xmlFile.addAttrAndTextContent("source[0]/float_array",
                new String[]{
                        "id=Plane-tex-mesh-positions-array",
                        "count=" + texTriangleManager.getUniquePointCount() * 3},
                texTriangleManager.getUniquePointString(true));
        xmlFile.addAttributes("source[0]/technique_common/accessor", new String[]{
                "source=#Plane-tex-mesh-positions-array",
                "count=" + texTriangleManager.getUniquePointCount(),
                "stride=3"
        });
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=X", "type=float"});
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=Y", "type=float"});
        xmlFile.addAttributes("source[0]/technique_common/accessor/param[-1]",
                new String[]{"name=Z", "type=float"});

        // --- write the uvs
        xmlFile.addAttributes("source[-1]", new String[] {
                "id=Plane-tex-mesh-uvs"
        });
        xmlFile.addAttrAndTextContent("source[1]/float_array",
                new String[]{
                        "id=Plane-tex-mesh-uvs-array",
                        "count=" + (texTriangleManager.getUniqueUVCount() * 2)},
                texTriangleManager.getUniqueUVString(false));

        xmlFile.addAttributes("source[1]/technique_common/accessor", new String[]{
                "source=#Plane-tex-mesh-uvs-array",
                "count=" + texTriangleManager.getUniqueUVCount(),
                "stride=2"
        });
        xmlFile.addAttributes("source[1]/technique_common/accessor/param[-1]",
                new String[]{"name=S", "type=float"});
        xmlFile.addAttributes("source[1]/technique_common/accessor/param[-1]",
                new String[]{"name=T", "type=float"});

        // vertices (generic information)
        xmlFile.addAttributes("vertices", new String[] {
                "id=Plane-tex-mesh-vertices"
        });
        xmlFile.addAttributes("vertices/input", new String[] {
                "semantic=POSITION",
                "source=#Plane-tex-mesh-positions"
        });

        // write data
        xmlFile.setTopNode("polylist[-1]");
        xmlFile.addAttributes("", new String[] {
                "material=lambert1-material",
                "count=" + texTriangleManager.triangleCount()
        });
        xmlFile.addAttributes("input[-1]", new String[] {
                "semantic=VERTEX",
                "source=#Plane-tex-mesh-vertices",
                "offset=0"
        });
        xmlFile.addAttributes("input[-1]", new String[] {
                "semantic=TEXCOORD",
                "source=#Plane-tex-mesh-uvs",
                "offset=1",
                "set=0"
        });
        xmlFile.addTextContent("vcount", texTriangleManager.getTriangleGrouping());
        xmlFile.addTextContent("p", texTriangleManager.getTrianglePolygonList());

    }

    // -----------------------

    // initialize the XML file
    private void initXmlFile() {
        // basic information
        xmlFile.addAttributes("", new String[] {
                "xmlns=http://www.collada.org/2005/11/COLLADASchema",
                "version=1.4.1"});

        // basic information
        xmlFile.resetTopNode("asset");
        xmlFile.addTextContent("contributor/author", "VoxelShop User");
        xmlFile.addTextContent("contributor/authoring_tool", "VoxelShop V" + VitcoSettings.VERSION_ID);
        String now = DateTools.now("yyyy-MM-dd'T'HH:mm:ss");
        xmlFile.addTextContent("created", now);
        xmlFile.addTextContent("modified", now);
        xmlFile.addAttributes("unit", new String[] {"name=meter","meter=1"});
        // blender default (can not be changed since blender ignores it!)
        xmlFile.addTextContent("up_axis", "Z_UP");

        // =========================

        // will hold the used images (library)
        // (this will be deleted later on if there is no
        // texture image used for this dae file)
        xmlFile.resetTopNode("library_images");

        // will hold all the color "effect" information
        xmlFile.resetTopNode("library_effects");

        // will contain the materials linked to the color information
        xmlFile.resetTopNode("library_materials");

        // will contain geometries (mesh) of the object
        xmlFile.resetTopNode("library_geometries");

        // =========================

        // contains the scene
        xmlFile.resetTopNode("library_visual_scenes/visual_scene");
        xmlFile.addAttributes("", new String[] {
                "id=Scene",
                "name=Scene"
        });

        // link the library_visual_scenes node
        xmlFile.resetTopNode("scene");
        xmlFile.addAttributes("instance_visual_scene", new String[]{"url=#Scene"});
    }

    // save this file
    public boolean writeToFile(File file, ErrorHandlerInterface errorHandler) {
        return xmlFile.writeToFile(file, errorHandler);
    }

}
