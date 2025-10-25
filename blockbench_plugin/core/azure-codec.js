/**
 * AzureLib Animator — Codec & Format Registration
 * -----------------------------------------------
 * Defines the Blockbench model format for AzureLib (.geo / .animation)
 * and handles project load, save, and import/export utilities.
 *
 * © 2025 AzureDoom — MIT License
 */

import omit from 'lodash/omit';
import AzureConfig, { DEFAULT_CONFIG, onSettingsChanged } from './azure-settings.js';
import { EASING_DEFAULT, hasArgs } from '../animation/azure-easing.js';
import { injectOverride, PatchRegistry, invertMolang as localInvert } from './azure-utils.js';
import { registerKeyframeOverrides, unregisterKeyframeOverrides } from '../animation/azure-keyframes.js';

const invertMolang = globalThis.invertMolang || localInvert;
let hasNotifiedConversion = false;

// ---------------------------------------------------------------------------
// Lifecycle registration
// ---------------------------------------------------------------------------

export function registerAzureCodec() {
  Codecs.project.on('compile', handleProjectCompile);
  Codecs.project.on('parse', handleProjectParse);
  Codecs.bedrock.on('compile', handleBedrockCompile);

  injectOverride(Animator, null, 'buildFile', buildAnimationFile);
  injectOverride(Animator, null, 'loadFile', loadAnimationFile);

  registerKeyframeOverrides();

  Blockbench.on('close_project', resetDefaults);
  Blockbench.on('new_project', resetDefaults);

  console.log('[AzureLib] Azure codec + keyframe overrides registered');
}

export function unregisterAzureCodec() {
  Codecs.project.events.compile.remove(handleProjectCompile);
  Codecs.project.events.parse.remove(handleProjectParse);
  Codecs.bedrock.events.compile.remove(handleBedrockCompile);

  unregisterKeyframeOverrides();

  format.delete();
  console.log('[AzureLib] Azure codec unregistered');
}

function resetDefaults() {
  Object.assign(AzureConfig, DEFAULT_CONFIG);
  hasNotifiedConversion = false;
  console.log('[AzureLib] Settings reset to defaults');
}

// ---------------------------------------------------------------------------
// Handlers for compilation/parsing hooks
// ---------------------------------------------------------------------------

function handleProjectCompile(event) {
  if (Format.id !== 'azure_model') return;

  if (AzureConfig.objectType === 'AZURE_ITEM_BLOCK') {
    AzureConfig.objectType = 'AZURE_ENTITY';
    AzureConfig.entityType = 'Entity/Block/Item';
  }

  event.model.azurelibSettings = AzureConfig;
}

function handleProjectParse(event) {
  const model = event.model || {};
  const formatId = model?.meta?.model_format || model?.model_format;
  if (formatId !== 'azure_model') return;

  const settings = model.azurelibSettings;
  if (settings && typeof settings === 'object') {
    const wasDeprecated = settings.objectType === 'AZURE_ITEM_BLOCK';
    Object.assign(AzureConfig, omit(settings, ['formatVersion']));

    if (wasDeprecated) {
      AzureConfig.objectType = 'AZURE_ENTITY';
      AzureConfig.entityType = 'Entity/Block/Item';
      console.warn('[AzureLib] Converted deprecated AZURE_ITEM_BLOCK → AZURE_ENTITY');
    }

    onSettingsChanged();

    if (wasDeprecated && !hasNotifiedConversion) {
      hasNotifiedConversion = true;
      Blockbench.showMessageBox({
        title: 'AzureLib Conversion',
        message:
          'This project used an older "Block/Item" type.\n\nIt has been updated to the unified "Entity/Block/Item" type (AZURE_ENTITY).',
        buttons: ['OK'],
      });
    }
  } else if (formatId === 'azure_model' && !model.azurelibSettings) {
    console.debug('[AzureLib] Azure model detected but no settings loaded yet.');
  }

  // Invert corrections for BB 5.0+
  if (Blockbench.isNewerThan('4.99') && model.animations) {
    console.log('[AzureLib] Applying legacy animation inversion fix for Blockbench 5.0+');
    for (const [name, anim] of Object.entries(model.animations)) {
      for (const bone of Object.values(anim.bones || {})) {
        for (const [channel, frames] of Object.entries(bone)) {
          if (channel === 'rotation' || channel === 'position') {
            for (const kf of Object.values(frames)) {
              if (kf?.vector) {
                if (channel === 'rotation') {
                  kf.vector[0] = invertMolang(kf.vector[0]);
                  kf.vector[1] = invertMolang(kf.vector[1]);
                } else if (channel === 'position') {
                  kf.vector[0] = invertMolang(kf.vector[0]);
                }
              }
            }
          }
        }
      }
    }
  }
}

function handleBedrockCompile(event) {
  if (Format.id !== 'azure_model') return;
  const geometry = event.model?.['minecraft:geometry'];
  if (geometry) {
    geometry.forEach(geo => {
      delete geo.item_display_transforms;
    });
  }
}

// ---------------------------------------------------------------------------
// Animator overrides
// ---------------------------------------------------------------------------

function buildAnimationFile() {
  const res = PatchRegistry.get(Animator).buildFile.apply(this, arguments);
  if (Format.id !== 'azure_model') return res;

  Object.assign(res, { azurelib_format_version: AzureConfig.formatVersion });
  const animations = res.animations;
  if (!animations) return res;

  const flipArray = (array, channel) => {
    if (!Array.isArray(array)) return array;
    if (channel === 'position') array[0] = invertMolang(array[0]);
    if (channel === 'rotation') {
      array[0] = invertMolang(array[0]);
      array[1] = invertMolang(array[1]);
    }
    return array;
  };

  // === Export fix + easing passthrough ===
  for (const animationName in animations) {
    const bones = animations[animationName]?.bones;
    if (!bones) continue;

    for (const boneName in bones) {
      const bone = bones[boneName];
      for (const channel in bone) {
        const group = bone[channel];
        for (const timestamp in group) {
          let kf = group[timestamp];
          if (!kf) continue;

          const merged = kf.pre || kf.post;
          if (merged) {
            if (merged.easing && !kf.easing) kf.easing = merged.easing;
            if (merged.easingArgs && !kf.easingArgs) kf.easingArgs = merged.easingArgs;
            if (merged.vector && !kf.vector) kf.vector = merged.vector;
            Object.assign(kf, merged);
          }

          if (!kf.easing && kf.__keyframe_ref && kf.__keyframe_ref.easing) {
            kf.easing = kf.__keyframe_ref.easing;
            if (kf.__keyframe_ref.easingArgs) kf.easingArgs = kf.__keyframe_ref.easingArgs;
          }

          delete kf.pre;
          delete kf.post;

          // Invert corrections for BB 5.0+
          if (Blockbench.isNewerThan('4.99')) {
            if (Array.isArray(kf)) {
              flipArray(kf, channel);
            } else if (Array.isArray(kf.vector)) {
              kf.vector = flipArray(kf.vector, channel);
            } else if (kf.x !== undefined || kf.y !== undefined || kf.z !== undefined) {
              if (channel === 'rotation') {
                kf.x = invertMolang(kf.x);
                kf.y = invertMolang(kf.y);
              } else if (channel === 'position') {
                kf.x = invertMolang(kf.x);
              }
            }
          }

          // Wrap arrays/numbers into vectors
          if (Array.isArray(kf)) {
            group[timestamp] = { vector: kf, easing: kf.easing, easingArgs: kf.easingArgs };
          } else if (typeof kf === 'number') {
            group[timestamp] = { vector: [kf, kf, kf], easing: kf.easing, easingArgs: kf.easingArgs };
          } else {
            group[timestamp] = kf;
          }
        }
      }
    }
  }

  // === Normalize static transforms ===
  for (const animName in animations) {
    const bones = animations[animName]?.bones;
    if (!bones) continue;
    for (const boneName in bones) {
      const bone = bones[boneName];
      for (const channel in bone) {
        const val = bone[channel];
        if (channel === 'scale') {
          if (typeof val === 'number') bone[channel] = { vector: [val, val, val] };
          else if (Array.isArray(val)) bone[channel] = { vector: val };
        }
      }
    }
  }

  return res;
}

// ---------------------------------------------------------------------------
// Load animation file (unchanged except import fixes)
// ---------------------------------------------------------------------------

function loadAnimationFile(file, filter) {
  if (Format?.id !== 'azure_model') {
    return PatchRegistry.get(Animator).loadFile.apply(this, arguments);
  }

  const json = file.json || autoParseJSON(file.content);
  if (!json || typeof json.animations !== 'object') return [];

  const animationsOut = [];

  const getPoints = (source, channel) => {
    const applyFlip = (vec, channel) => {
      if (!vec) return vec;
      if (channel === 'position') vec.x = invertMolang(vec.x);
      if (channel === 'rotation') {
        vec.x = invertMolang(vec.x);
        vec.y = invertMolang(vec.y);
      }
      return vec;
    };
	
    if (source && typeof source === 'object' && Array.isArray(source.vector)) {
      return getPoints(source.vector, channel);
    }

    if (Array.isArray(source) && Blockbench.isNewerThan('4.99')) {
      const vec = { x: source[0], y: source[1], z: source[2] };
      applyFlip(vec, channel);
      return [vec];
    } else if (typeof source === 'number' || typeof source === 'string') {
      return [{ x: source, y: source, z: source }];
    } else if (source && typeof source === 'object') {
      if (Array.isArray(source.vector)) return getPoints(source.vector, channel);
      const arr = [];
      if (source.pre) arr.push(getPoints(source.pre, channel)[0]);
      if (source.post) {
        const post = getPoints(source.post, channel)[0];
        arr.push(post);
      }
      return arr.length ? arr : undefined;
    }
    return undefined;
  };

  for (const name in json.animations) {
    if (filter && !filter.includes(name)) continue;
    const src = json.animations[name];

    const anim = new Animation({
      name,
      path: file.path,
      loop:
        src.loop === true
          ? 'loop'
          : src.loop === 'hold_on_last_frame'
          ? 'hold'
          : src.loop,
      override: src.override_previous_animation,
      anim_time_update:
        typeof src.anim_time_update === 'string'
          ? src.anim_time_update.replace(/;(?!$)/, ';\n')
          : src.anim_time_update,
      blend_weight:
        typeof src.blend_weight === 'string'
          ? src.blend_weight.replace(/;(?!$)/, ';\n')
          : src.blend_weight,
      length: src.animation_length,
    }).add();

    if (src.bones) {
      for (const boneName in src.bones) {
        const bone = src.bones[boneName];
        const group = Group.all.find(g => g.name.toLowerCase() === boneName.toLowerCase());
        const uuid = group ? group.uuid : guid();
        const animator = new BoneAnimator(uuid, anim, boneName);
        anim.animators[uuid] = animator;

        for (const channel in bone) {
          if (!Animator.possible_channels[channel]) continue;
          const channelData = bone[channel];

          const addKeyframe = (time, data) => {
            const pts = getPoints(data, channel);
            if (!pts) return;
            const easingValue = data?.easing ?? channelData?.easing ?? EASING_DEFAULT;
            animator.addKeyframe({
              time,
              channel,
              easing: easingValue,
              easingArgs:
                Array.isArray(data?.easingArgs) && data.easingArgs.length
                  ? data.easingArgs
                  : Array.isArray(channelData?.easingArgs)
                  ? channelData.easingArgs
                  : undefined,
              interpolation: data?.lerp_mode,
              uniform: !(Array.isArray(data) || Array.isArray(data?.vector)),
              data_points: pts,
            });
          };

          if (
            typeof channelData === 'string' ||
            typeof channelData === 'number' ||
            Array.isArray(channelData)
          ) {
            addKeyframe(0, channelData);
          } else if (channelData && typeof channelData === 'object') {
            if (channelData.post || channelData.pre || channelData.vector !== undefined) {
              addKeyframe(0, channelData);
            } else {
              for (const t in channelData) {
                const entry = channelData[t];
                if (entry && typeof entry === 'object') {
                  if (Array.isArray(entry.vector)) {
                    addKeyframe(parseFloat(t), entry);
                  } else if (Array.isArray(entry)) {
                    addKeyframe(parseFloat(t), { vector: entry });
                  } else {
                    addKeyframe(parseFloat(t), entry);
                  }
                } else {
                  addKeyframe(parseFloat(t), entry);
                }
              }
            }
          }
        }
      }
    }

    anim.calculateSnappingFromKeyframes();
    if (!Animation.selected && Animator.open) anim.select();
    animationsOut.push(anim);
  }

  return animationsOut;
}

// ---------------------------------------------------------------------------
// Export helpers (Display JSON handling)
// ---------------------------------------------------------------------------

export function maybeExportItemJson(options = {}, as) {
  const should = (key, fallback) => (options[key] === undefined ? fallback : options[key]);
  const model = {};
  if (should('comment', settings.credit.value)) model.credit = settings.credit.value;
  model.parent = 'builtin/entity';
  if (should('ambientocclusion', Project.ambientocclusion === false)) model.ambientocclusion = false;
  if (Project.texture_width !== 16 || Project.texture_height !== 16)
    model.texture_size = [Project.texture_width, Project.texture_height];
  if (should('front_gui_light', Project.front_gui_light)) model.gui_light = 'front';
  if (should('overrides', Project.overrides)) model.overrides = Project.overrides;

  if (should('display', Object.keys(Project.display_settings).length >= 1)) {
    const display = {};
    for (const key in DisplayMode.slots) {
      const slot = DisplayMode.slots[key];
      if (DisplayMode.slots.hasOwnProperty(key) && Project.display_settings[slot]?.export)
        display[slot] = Project.display_settings[slot].export();
    }
    if (Object.keys(display).length) {
      model.display = display;
      const OFFSET_Y = 4.0;
      for (const [slot, data] of Object.entries(display)) {
        const tr = data?.translation;
        if (Array.isArray(tr)) tr[1] = Math.round((Number(tr[1] || 0) - OFFSET_Y) * 100) / 100;
      }
    }
  }

  if (Project.textures && should('textures', Object.keys(Project.textures).length >= 1)) {
    for (const tex of Object.values(Project.textures)) {
      if (tex.particle || Object.keys(Project.textures).length === 1) {
        let name = tex.name.replace('.png', '');
        if (/^[_\-.a-z0-9/]+$/.test(name)) model.textures = { particle: name };
        break;
      }
    }
  }

  const jsonStr = JSON.stringify(model, null, 2);
  const path = AzureConfig.itemModelPath;

  Blockbench.export(
    {
      resource_id: 'model',
      type: Codecs.java_block.name,
      extensions: ['json'],
      name: codec.fileName().replace('.geo', '.item'),
      startpath: path,
      content: jsonStr,
    },
    realPath => {
      AzureConfig.itemModelPath = realPath;
    }
  );

  return this;
}

export function maybeImportItemJson() {
  Blockbench.import(
    {
      resource_id: 'model',
      type: 'json',
      extensions: ['json'],
      readtype: 'text',
      multiple: false,
    },
    files => {
      if (!files?.[0]) return;
      let json;
      try {
        json = JSON.parse(files[0].content);
      } catch {
        return Blockbench.showQuickMessage('[AzureLib] Invalid JSON file.');
      }

      if (json.parent !== 'builtin/entity' || typeof json.display !== 'object')
        return Blockbench.showQuickMessage('[AzureLib] Not a valid AzureLib display file.');

      Project.display_settings = {};
      const OFFSET_Y = 4.0; // same offset used in maybeExportItemJson
      for (const [slot, data] of Object.entries(json.display)) {
        if (!DisplayMode.slots.includes(slot)) continue;

        const rotation = Array.isArray(data.rotation) ? data.rotation.slice() : [0, 0, 0];
        const translation = Array.isArray(data.translation) ? data.translation.slice() : [0, 0, 0];
        const scale = Array.isArray(data.scale) ? data.scale.slice() : [1, 1, 1];

        // Apply offset correction back into Blockbench space
        translation[1] = Math.round((Number(translation[1] || 0) + OFFSET_Y) * 100) / 100;

        const slotObj = new DisplaySlot(slot);
        Object.assign(slotObj, { rotation, translation, scale });
        Project.display_settings[slot] = slotObj;
      }

      Project.saved = false;
      DisplayMode.vue.$forceUpdate();
      DisplayMode.updateDisplayBase();
      Blockbench.showQuickMessage('[AzureLib] Display settings imported.');
    }
  );
}


// ---------------------------------------------------------------------------
// Model format registration
// ---------------------------------------------------------------------------

const codec = Codecs.bedrock;

export const format = new ModelFormat({
  id: 'azure_model',
  name: 'AzureLib Animated Model',
  category: 'minecraft',
  description: 'Animated model for Java mods using AzureLib.',
  icon: 'view_in_ar',
  rotate_cubes: true,
  box_uv: true,
  optional_box_uv: true,
  single_texture: true,
  bone_rig: true,
  centered_grid: true,
  animated_textures: true,
  select_texture_for_particles: true,
  animation_mode: true,
  animation_files: true,
  locators: true,
  codec: Codecs.project,
  display_mode: true,
  onActivation() {},
});

export default codec;